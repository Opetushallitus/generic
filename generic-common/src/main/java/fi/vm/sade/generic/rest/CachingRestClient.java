package fi.vm.sade.generic.rest;

import com.google.gson.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import fi.vm.sade.authentication.cas.CasClient;
import fi.vm.sade.generic.healthcheck.HealthChecker;
import fi.vm.sade.generic.ui.portlet.security.ProxyAuthenticator;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
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
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.httpclient.HttpStatus.*;

/**
 * Simple http client, that allows doing GETs to REST-resources so that http cache headers are respected.
 * Just a lightweight wrapper on top of apache commons-http and commons-http-cache.
 * Use get -method to do requests.
 *
 * Service-as-a-user authentication: set webCasUrl/casService/username/password
 *
 * Proxy authentication: set useProxyAuthentication=true + casService
 *
 * @author Antti Salonen
 */
public class CachingRestClient implements HealthChecker {

    public static final String WAS_REDIRECTED_TO_CAS = "redirected_to_cas";
    public static final int DEFAULT_TIMEOUT_MS = 5 * 60 * 1000; // 5min
    private static final Charset UTF8 = Charset.forName("UTF-8");
    protected static Logger logger = LoggerFactory.getLogger(CachingRestClient.class);
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

    private PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
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
    protected String serviceAsAUserTicket;
    private ProxyAuthenticator proxyAuthenticator;
    private boolean useProxyAuthentication = false;
    @Value("${auth.mode:cas}")
    private String proxyAuthMode;
    private String requiredVersionRegex;
    private final int timeoutMs;

    public CachingRestClient() {
        this(DEFAULT_TIMEOUT_MS);
    }

    public CachingRestClient(int timeoutMs) {
        this.timeoutMs = timeoutMs;

        // multithread support + max connections
        connectionManager.setDefaultMaxPerRoute(100); // default 2
        connectionManager.setMaxTotal(1000); // default 20

        // cache config
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxCacheEntries(50 * 1000);
        cacheConfig.setMaxObjectSize(10 * 1024 * 1024); // 10M, eg oppilaitosnumero -koodisto is 7,5M

        // init stuff
        final DefaultHttpClient actualClient = new DefaultHttpClient(connectionManager);

        HttpParams httpParams = actualClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeoutMs);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);

        actualClient.setRedirectStrategy(new DefaultRedirectStrategy(){
            // detect redirects to cas
            @Override
            public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
                URI locationURI = super.getLocationURI(request, response, context);
                String uri = locationURI.toString();
                if (isCasUrl(uri)) {
                    logger.debug("set redirected_to_cas=true, url: " + uri);
                    context.setAttribute(WAS_REDIRECTED_TO_CAS, "true");
                    clearRedirects();
                } else { // when redirecting back to service _from_ cas
                    logger.debug("set redirected_to_cas=false, url: " + uri);
                    context.removeAttribute(WAS_REDIRECTED_TO_CAS);
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
        gsonBuilder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
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
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }

    public String getAsString(String url) throws IOException {
        return IOUtils.toString(get(url));
    }

    private <T> T fromJson(Class<? extends T> resultType, String response) throws IOException {
        try {
            return gson.fromJson(response, resultType);
        } catch (JsonSyntaxException e) {
            throw new IOException("failed to parse object from (json) response, type: "+resultType.getSimpleName()+", reason: "+e.getCause()+", response:\n"+response);
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
        return "true".equals(localContext.get().getAttribute(WAS_REDIRECTED_TO_CAS));
    }

    protected boolean authenticate(final HttpRequestBase req) throws IOException {

        // username / password authentication

        if (useServiceAsAUserAuthentication()) {
            // get ticket
            if (serviceAsAUserTicket == null) {
                checkNotNull(username, "username");
                checkNotNull(password, "password");
                checkNotNull(webCasUrl, "webCasUrl");
                checkNotNull(casService, "casService");
                serviceAsAUserTicket = obtainNewCasServiceAsAUserTicket();
                logger.debug("got new serviceAsAUser ticket, service: "+casService+", ticket: "+serviceAsAUserTicket);
            }
            // attach ticket
            //addRequestParameter(req, "ticket", serviceAsAUserTicket);
            req.addHeader("CasSecurityTicket", serviceAsAUserTicket);
            return true;
        }

        // proxy authentication

        else if (useProxyAuthentication) {

            checkNotNull(webCasUrl, "webCasUrl");
            checkNotNull(casService, "casService");

            if (proxyAuthenticator == null) {
                proxyAuthenticator = new ProxyAuthenticator();
            }
            final boolean[] gotNewProxyTicket = {false};
            proxyAuthenticator.proxyAuthenticate(casService, proxyAuthMode, new ProxyAuthenticator.Callback() {
                @Override
                public void setRequestHeader(String key, String value) {
                    req.addHeader(key, value);
                }
                @Override
                public void gotNewTicket(Authentication authentication, String proxyTicket) {
                    logger.debug("got new proxy ticket, service: "+casService+", ticket: "+proxyTicket);
                    gotNewProxyTicket[0] = true;
                }
            });
            return gotNewProxyTicket[0];
        }

        return false;
    }

    private void checkNotNull(String value, String name) {
        if (value == null) throw new NullPointerException("CachingRestClient."+name+" is null, and guess what, it shouldn't!");
    }

    /*
    private void addRequestParameter(HttpRequestBase req, String key, String value) {
        URIBuilder builder = new URIBuilder(req.getURI()).setParameter(key, value);
        try {
            req.setURI(builder.build());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    */

    private boolean useServiceAsAUserAuthentication() {
        return username != null;
    }

    protected String obtainNewCasServiceAsAUserTicket() throws IOException {
        return CasClient.getTicket(webCasUrl + "/v1/tickets", username, password, casService);
    }

    public String postForLocation(String url, String content) throws IOException {
        return postForLocation(url, "application/json", content);
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

    public HttpResponse delete(String url) throws IOException {
        return execute(new HttpDelete(url), null, null);
    }

    public HttpResponse execute(HttpRequestBase req, String contentType, String postOrPutContent) throws IOException {
        return execute(req, contentType, postOrPutContent, 0);
    }

    public HttpResponse execute(HttpRequestBase req, String contentType, String postOrPutContent, int retry) throws IOException {
        // prepare
        if (req.getURI().toString().startsWith("/") && casService != null) { // if relative url
            try {
                req.setURI(new URIBuilder(casService.replace("/j_spring_cas_security_check", "") + req.getURI().toString()).build());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        String url = req.getURI().toString();
        if (req.getURI().getHost() == null) throw new NullPointerException("CachingRestClient.execute ERROR! host is null, req.uri: "+url);
        if (contentType != null) {
            req.setHeader("Content-Type", contentType);
        }
        if (postOrPutContent != null && req instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase)req).setEntity(new StringEntity(postOrPutContent, UTF8));
        }

        // authenticate if needed
        boolean wasJustAuthenticated = authenticate(req);

        // do actual request
        HttpResponse response = null;
        String responseString = null;
        try {
            response = cachingClient.execute(req, localContext.get());
        } catch (Exception e) {
            logger.error("error in CachingRestClient - " + info(req, response, wasJustAuthenticated, wasJustAuthenticated, wasJustAuthenticated, retry), e);
            throw new IOException("Internal error calling "+req.getMethod()+"/"+url+" (check logs): "+e.getMessage());
        } finally {
            // after request, wrap response entity so it can be accessed later, and release the connection
            if (response != null && response.getEntity() != null) {
                responseString = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                response.setEntity(new StringEntity(responseString, "UTF-8"));
            }
            req.releaseConnection();
        }

        // debug logging
        boolean isRedirCas = isRedirectToCas(response); // this response is 302 with location header pointing to cas
        boolean wasRedirCas = wasRedirectedToCas(); // this response is from cas after 302 redirect
        boolean isHttp401 = response.getStatusLine().getStatusCode() == SC_UNAUTHORIZED;
        if (logger.isDebugEnabled()) {
            logger.debug(info(req, response, wasJustAuthenticated, isRedirCas, wasRedirCas, retry));
            logger.debug("    responseString: "+responseString);
        }

        // just got new valid ticket, but still got cas login page.. something wrong with the system, target service didn't process the request/ticket correctly?
        if (retry > 0 && wasJustAuthenticated && (isRedirCas || wasRedirCas)) {
            throw new IOException("just got new valid ticket, but still got cas login page.. something wrong with the system, target service didn't process the request/ticket correctly?\n"
                    +info(req, response, wasJustAuthenticated, isRedirCas, wasRedirCas, retry));
        }

        // authentication: was redirected to cas OR http 401 -> get ticket and retry once (but do it only once, hence 'retry')
        // todo: onko hyvä että aina koitetaan kerran uusiksi 401 virheen jälkeen? jos esim ticket vanhentunut, tuleeko sieltä edes 401 koskaan?
        if (isRedirCas || wasRedirCas || isHttp401) {
            if (retry == 0) {
                logger.warn("warn! got redirect to cas or 401 unauthorized, re-getting ticket and retrying request");
                clearTicket(); // will force to get new ticket on execute
                logger.debug("set redirected_to_cas=false");
                localContext.get().removeAttribute(WAS_REDIRECTED_TO_CAS);
                return execute(req, contentType, postOrPutContent, 1);
            } else { // if already retried, 401 unauthorized is for real!
                logger.error("Error calling REST resource, got redirect to cas or 401 unauthorized, status: "+response.getStatusLine()+", url: "+req.getURI());
                throw new HttpException(req, response);
            }
        }

        if(response.getStatusLine().getStatusCode() >= SC_INTERNAL_SERVER_ERROR) {
            logger.error("Error calling REST resource, status: "+response.getStatusLine()+", url: "+req.getURI());
            throw new HttpException(req, response);
        }

        if(response.getStatusLine().getStatusCode() >= SC_NOT_FOUND) {
            logger.error("Error calling REST resource, status: "+response.getStatusLine()+", url: "+req.getURI());
            throw new HttpException(req, response);
        }

        cacheStatus = localContext.get().getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);

        logger.debug("{}, url: {}, contentType: {}, content: {}, status: {}, headers: {}", new Object[]{req.getMethod(), url, contentType, postOrPutContent, response.getStatusLine(), Arrays.asList(response.getAllHeaders())});
        return response;
    }

    private String info(HttpRequestBase req, HttpResponse response, boolean wasJustAuthenticated, boolean isRedirCas, boolean wasRedirCas, int retry) {
        return "url: "+ req.getURI()+", method: "+req.getMethod()+", serviceauth: " + useServiceAsAUserAuthentication() + ", proxyauth: "+useProxyAuthentication+", currentuser: "+getCurrentUser()+", isredircas: "+ isRedirCas +", wasredircas: " + wasRedirCas + ", status: " + response.getStatusLine().getStatusCode() + ", wasJustAuthenticated: " + wasJustAuthenticated+", retry: "+retry+", timeoutMs: "+timeoutMs;
    }

    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext() != null ? SecurityContextHolder.getContext().getAuthentication() : null;
        return authentication != null ? authentication.getName() : null;
    }

    /** will force to get new ticket next time */
    private void clearTicket() {
        serviceAsAUserTicket = null;
        if (useProxyAuthentication && proxyAuthenticator != null) {
            proxyAuthenticator.clearTicket(casService);
        }
    }

    private void clearRedirects() {
        // clear redirects, because cas auth could cause same auth redirections again after new login/ticket. this will prevent CircularRedirectException
        localContext.get().setAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS, new RedirectLocations());
        logger.info("cleared redirects");
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
        clearTicket();
        this.webCasUrl = webCasUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        clearTicket();
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        clearTicket();
        this.password = password;
    }

    public String getCasService() {
        return casService;
    }

    public void setCasService(String casService) {
        clearTicket();
        this.casService = casService;
    }

    /** Check health of this rest client */
    @Override
    public Object checkHealth() throws Throwable {
        if (casService != null) {
            // call target service's buildversion url (if we have credentials try the secured url)
            final String buildversionUrl = casService.replace("/j_spring_cas_security_check", "") + "/buildversion.txt" + (useServiceAsAUserAuthentication() ? "?auth" : "");
            final HttpResponse result = execute(new HttpGet(buildversionUrl), null, null);

            LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>() {{
                put("url", buildversionUrl);
                put("user", useServiceAsAUserAuthentication() ? username : useProxyAuthentication ? "proxy" : "anonymous");
                put("status", result.getStatusLine().getStatusCode() == 200 ? "OK" : result.getStatusLine());
                // todo: kuormitusdata?
            }};

            // mikäli kohdepalvelu ok, mutta halutaan varmistaa vielä sen versio
            if (result.getStatusLine().getStatusCode() == 200 && requiredVersionRegex != null) {
                Properties buildversionProps = new Properties();
                buildversionProps.load(result.getEntity().getContent());
                String version = buildversionProps.getProperty("version");
                if (!version.matches(requiredVersionRegex)) {
                    throw new Exception("wrong version: "+version+", required: "+ requiredVersionRegex+", service: "+casService);
                }
                map.put("version", version);
            }

            return map;
        } else {
            return "nothing to check";
        }
    }

    public boolean isUseProxyAuthentication() {
        return useProxyAuthentication;
    }

    public void setUseProxyAuthentication(boolean useProxyAuthentication) {
        this.useProxyAuthentication = useProxyAuthentication;
    }

    public ProxyAuthenticator getProxyAuthenticator() {
        return proxyAuthenticator;
    }

    public void setProxyAuthenticator(ProxyAuthenticator proxyAuthenticator) {
        this.proxyAuthenticator = proxyAuthenticator;
    }

    public String getRequiredVersionRegex() {
        return requiredVersionRegex;
    }

    public void setRequiredVersionRegex(String requiredVersionRegex) {
        this.requiredVersionRegex = requiredVersionRegex;
    }

    public static class HttpException extends IOException {

        private int statusCode;
        private String statusMsg;
        private String errorContent;

        public HttpException(HttpRequestBase req, HttpResponse response) {
            super("Error calling REST resource, status: "+response.getStatusLine()+", url: "+req.getURI());
            this.statusCode = response.getStatusLine().getStatusCode();
            this.statusMsg = response.getStatusLine().getReasonPhrase();
            try {
                this.errorContent = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                CachingRestClient.logger.error("error reading errorContent: "+e, e);
            }
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusMsg() {
            return statusMsg;
        }

        public String getErrorContent() {
            return errorContent;
        }
    }
}
