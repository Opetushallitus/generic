package fi.vm.sade.generic.rest;

import com.google.gson.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import fi.vm.sade.authentication.cas.CasClient;
import fi.vm.sade.generic.healthcheck.HealthChecker;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

/**
 * Simple http client, that allows doing GETs to REST-resources so that http cache headers are respected.
 * Just a lightweight wrapper on top of apache commons-http and commons-http-cache.
 * Use get -method to do requests.
 *
 * @author Antti Salonen
 */
public class CachingRestClient implements HealthChecker {

    public static final String WAS_REDIRECTED_TO_CAS = "redirected_to_cas";
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private static ThreadLocal<DateFormat> df1 = new ThreadLocal<DateFormat>(){
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
    };
    private static ThreadLocal<DateFormat> df2 = new ThreadLocal<DateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
        
    };

    private HttpClient cachingClient;
    private ThreadLocal<HttpContext> localContext = new ThreadLocal<HttpContext>(){
        @Override
        protected HttpContext initialValue() {
            return new BasicHttpContext();
        }
    };
    //private HttpResponse response;
    private Object cacheStatus;  //used in tests
    private Gson gson;

    private String webCasUrl;
    private String username;
    private String password;
    private String casService;
    protected String ticket;

    public CachingRestClient() {
        // multithread support + max connections
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(100); // default 2
        connectionManager.setMaxTotal(1000); // default 20

        // cache config
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(50 * 1000);
        cacheConfig.setMaxObjectSize(10 * 1024 * 1024); // 10M, eg oppilaitosnumero -koodisto is 7,5M

        // init stuff
        final DefaultHttpClient actualClient = new DefaultHttpClient(connectionManager);
        actualClient.setRedirectStrategy(new DefaultRedirectStrategy(){
            // detect redirects to cas
            @Override
            public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                URI locationURI = super.getLocationURI(request, response, context);
                String uri = locationURI.toString();
                if (isCasUrl(uri)) {
                    logger.debug("is cas redirect: " + uri);
                    context.setAttribute(WAS_REDIRECTED_TO_CAS, "true");
                }
                return locationURI;
            }
        });
        cachingClient = new CachingHttpClient(actualClient, cacheConfig);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class, new JsonDeserializer<XMLGregorianCalendar>() {

            @Override
            public XMLGregorianCalendar deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
                    throws JsonParseException {
                String string = json.getAsString();
                try {
                 return parseXmlGregorianCalendar(string);
                } catch (Throwable t){
                    return null;
                }
            }

        });
        gson = gsonBuilder.create();
    }

    private boolean isCasUrl(String uri) {
        return uri != null && (uri.endsWith("/cas") || uri.contains("/cas/") || uri.contains("/cas?"));
    }

    /**
     * get REST Json resource as Java object of type resultType (deserialized with gson).
     * Returns null if error occurred while querying resource.
     */
    public <T> T get(String url, Class<? extends T> resultType) throws IOException {
        InputStream is = null;
        String response = null;
        try {
            is = get(url);
            response = IOUtils.toString(is);
            T t = fromJson(resultType, response);
            return t;
        } catch (JsonObjectException e) {
            return null;
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }

    private <T> T fromJson(Class<? extends T> resultType, String response) throws IOException {
        try {
            return gson.fromJson(response, resultType);
        } catch (Exception e) {
            throw new IOException("failed to convert response to json, response: "+response);
        }
    }

    /**
     * get REST Json resource as string.
     */
    public InputStream get(String url) throws IOException {
        HttpGet req = new HttpGet(url);
        HttpResponse response = execute(req, null, null);
        return response.getEntity().getContent();
    }

    private boolean wasRedirectedToCas() {
        return "true".equals(localContext.get().getAttribute("redirected_to_cas"));
    }

    protected boolean authenticate(HttpRequestBase req) throws IOException {
        if(isAuthenticable() && ticket == null) {
            ticket = obtainNewCasTicket();
            if (ticket == null) {
                throw new IOException("failed to get ticket, check credentials! user: "+username+", cas: "+webCasUrl+", service: "+casService);
            }
            if (req.getURI().toString().contains("ticket=")) {
                throw new IOException("uri already has a ticket: "+req.getURI());
            }
            URIBuilder builder = new URIBuilder(req.getURI()).addParameter("ticket", ticket);
            try {
                req.setURI(builder.build());
            } catch (URISyntaxException e) {
                logger.error("URI syntax incorrect." , e);
            }
            return true;
        }
        return false;
    }

    private boolean isAuthenticable() {
        return webCasUrl != null && username != null && password != null && casService != null;
    }

    protected String obtainNewCasTicket() throws IOException {
        return CasClient.getTicket(webCasUrl + "/v1/tickets", username, password, casService);
    }

    public String postForLocation(String url, String contentType, String content) throws IOException {
        HttpResponse response = post(url, contentType, content);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return response.getFirstHeader("Location").getValue();
        } else {
            throw new RuntimeException("post didn't result in http 201 created, status: "+response.getStatusLine()+", url: "+url);
        }
    }

    public HttpResponse post(String url, String contentType, String content) throws IOException {
        return execute(new HttpPost(url), contentType, content);
    }

    public HttpResponse put(String url, String contentType, String content) throws IOException {
        return execute(new HttpPut(url), contentType, content);
    }

    public HttpResponse execute(HttpRequestBase req, String contentType, String postOrPutContent) throws IOException {
        // prepare
        String url = req.getURI().toString();
        if (contentType != null) {
            req.setHeader("Content-Type", contentType);
        }
        if (postOrPutContent != null && req instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase)req).setEntity(new StringEntity(postOrPutContent));
        }

        // authenticated if needed
        boolean wasJustAuthenticated = authenticate(req);

        // do actual request
        final HttpResponse response = cachingClient.execute(req, localContext.get());

        // authentication: was redirected to cas OR http 401 -> get ticket and retry once (but do it only once, hence '!wasJustAuthenticated')
        logger.debug("url: "+url+", isauth: " + isAuthenticable() + ", isredir: "+isRedirectToCas(response)+" wasredir: " + wasRedirectedToCas() + ", status: " + response.getStatusLine().getStatusCode() + ", wasJustAuthenticated: " + wasJustAuthenticated);
        if (isAuthenticable() && (isRedirectToCas(response) || wasRedirectedToCas() || response.getStatusLine().getStatusCode() == 401) && !wasJustAuthenticated) {
            logger.warn("warn! got redirect to cas or 401 unauthorized, re-getting ticket and retrying request");
            ticket = null; // will force to get new ticket next time
            return execute(req, contentType, postOrPutContent);
        }

        if(response.getStatusLine().getStatusCode() == 401) {
            logger.warn("Wrong status code 401, clearing ticket.", response.getStatusLine().getStatusCode());
            ticket = null; // will force to get new ticket next time
            throw new IOException("got http 401 unauthorized, user: "+username+", url: "+url);
        }

        if(response.getStatusLine().getStatusCode() >= 500) {
            logger.error("Error calling REST resource, status: "+response.getStatusLine()+", url: "+req.getURI());
            throw new IOException("Error calling REST resource, status: "+response.getStatusLine()+", url: "+req.getURI());
        }

        cacheStatus = localContext.get().getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);

        logger.debug("{}, url: {}, contentType: {}, content: {}, status: {}, headers: {}", new Object[]{req.getMethod(), url, contentType, postOrPutContent, response.getStatusLine(), Arrays.asList(response.getAllHeaders())});
        return response;
    }

    private boolean isRedirectToCas(HttpResponse response) {
        Header location = response.getFirstHeader("Location");
        return location != null && isCasUrl(location.getValue());
    }

    public Object getCacheStatus() {
        return cacheStatus;
    }

    private XMLGregorianCalendar parseXmlGregorianCalendar(String string) {
        // long t = System.currentTimeMillis();
        if (string == null || string.isEmpty()) {
            return null;
        }

        final boolean hasSemicolon = string.indexOf(":") != -1;
        final boolean hasDash = string.indexOf("-") != -1;

        try {
            if (hasSemicolon) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(df1.get().parse(string));
                return new XMLGregorianCalendarImpl(cal);
            } else if (hasDash) {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(df2.get().parse(string));
                return new XMLGregorianCalendarImpl(cal);
            } else {
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(new Date(Long.parseLong(string)));
                return new XMLGregorianCalendarImpl(cal);

            }
        } catch (Throwable th) {
            logger.warn("error parsing json to xmlgregoriancal: " + string);
        }
        return null;
    }

    public String getWebCasUrl() {
        return webCasUrl;
    }

    public void setWebCasUrl(String webCasUrl) {
        this.ticket = null;
        this.webCasUrl = webCasUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.ticket = null;
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.ticket = null;
        this.password = password;
    }

    public String getCasService() {
        return casService;
    }

    public void setCasService(String casService) {
        this.ticket = null;
        this.casService = casService;
    }

    /** Check health of this rest client */
    @Override
    public Object checkHealth() throws Throwable {
        if (isAuthenticable()) {
            // call target service's buildversion url which requires authentication
            final String url = casService.replace("/j_spring_cas_security_check", "") + "/buildversion.txt?auth";
            final HttpResponse result = execute(new HttpGet(url), null, null);
            return new LinkedHashMap(){{
                put("url", url);
                put("user", username);
                put("status", result.getStatusLine().getStatusCode() == 200 ? "OK" : result.getStatusLine());
            }};
        } else {
            return "nothing to check - anonymous access";
        }
    }

}
