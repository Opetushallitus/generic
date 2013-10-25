package fi.vm.sade.generic.rest;

import com.google.gson.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import fi.vm.sade.authentication.cas.CasClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Simple http client, that allows doing GETs to REST-resources so that http cache headers are respected.
 * Just a lightweight wrapper on top of apache commons-http and commons-http-cache.
 * Use get -method to do requests.
 *
 * @author Antti Salonen
 */
public class CachingRestClient {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private static ThreadLocal<DateFormat> df1 = new ThreadLocal<DateFormat>(){
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm");
        };
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
    private String ticket;

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

    /**
     * get REST Json resource as Java object of type resultType (deserialized with gson).
     */
    public <T> T get(String url, Class<? extends T> resultType) throws IOException {
        InputStream is = null;
        try {
            is = get(url);
            T t = gson.fromJson(new InputStreamReader(is, "utf-8"), resultType);
            return t;
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }

    /**
     * get REST Json resource as string.
     */
    public InputStream get(String url) throws IOException {
//        logger.info("get... url: {}", url);
        final HttpGet httpget = new HttpGet(url);

        authenticate(httpget);

        final HttpResponse response = cachingClient.execute(httpget, localContext.get());

//        if(response.getStatusLine().getStatusCode() == 401) {
//            logger.warn("Wrong status code 401, clearing ticket.", response.getStatusLine().getStatusCode());
//            ticket = null;
//        }

        cacheStatus = localContext.get().getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);
//        logger.info("get done, url: {}, status: {}, cacheStatus: {}, headers: {}", new Object[]{url, response.getStatusLine(), null, response.getAllHeaders()});
//        System.out.println("==> get done, url: "+url+", status: "+response.getStatusLine()+", cacheStatus: "+cacheStatus+", headers: "+ Arrays.asList(response.getAllHeaders()));
//        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        return response.getEntity().getContent();
    }

    private void authenticate(HttpGet httpget) {
        if(webCasUrl != null && username != null && password != null && casService != null) {
            ticket = CasClient.getTicket(webCasUrl + "/v1/tickets", username, password, casService);
            URIBuilder builder = new URIBuilder(httpget.getURI()).addParameter("ticket", ticket);
            try {
                httpget.setURI(builder.build());
            } catch (URISyntaxException e) {
                logger.error("URI syntax incorrect." , e);
            }
        }
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
}
