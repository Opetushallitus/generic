package fi.vm.sade.generic.rest;

import com.google.gson.*;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.methods.HttpGet;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private HttpClient cachingClient;
    private HttpContext localContext;
    private HttpResponse response;
    private Object cacheStatus;
    private Gson gson;

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
        localContext = new BasicHttpContext();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(XMLGregorianCalendar.class, new JsonDeserializer<XMLGregorianCalendar>() {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            @Override
            public XMLGregorianCalendar deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
                    throws JsonParseException {
                try {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTime(df.parse(json.getAsString()));
                    return new XMLGregorianCalendarImpl(cal);
                } catch (ParseException e) {
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
        return gson.fromJson(new InputStreamReader(get(url)), resultType);
    }

    /**
     * get REST Json resource as string.
     */
    public InputStream get(String url) throws IOException {
//        logger.info("get... url: {}", url);
        HttpGet httpget = new HttpGet(url);
        response = cachingClient.execute(httpget, localContext);
        cacheStatus = (CacheResponseStatus) localContext.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);
        logger.info("get done, url: {}, status: {}, cacheStatus: {}, headers: {}", new Object[]{url, response.getStatusLine(), cacheStatus, response.getAllHeaders()});
//        System.out.println("==> get done, url: "+url+", status: "+response.getStatusLine()+", cacheStatus: "+cacheStatus+", headers: "+ Arrays.asList(response.getAllHeaders()));
//        System.out.println(IOUtils.toString(response.getEntity().getContent()));
        return response.getEntity().getContent();
    }

    public Object getCacheStatus() {
        return cacheStatus;
    }

}
