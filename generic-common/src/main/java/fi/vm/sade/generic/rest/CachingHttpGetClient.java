package fi.vm.sade.generic.rest;

import static org.apache.commons.httpclient.HttpStatus.SC_FORBIDDEN;
import static org.apache.commons.httpclient.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.commons.httpclient.HttpStatus.SC_NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Simple http client, that allows doing GETs to REST-resources so that http cache headers are respected.
 * Just a lightweight wrapper on top of apache commons-http and commons-http-cache.
 * Use get -method to do requests.
 *
 * Migrated from CachingRestClient by simplifying. No authentication support or ThreadLocals
 *
 * @author Antti Salonen
 * @author Juha Paananen
 */
public class CachingHttpGetClient {
    public static final int DEFAULT_TIMEOUT_MS = 5 * 60 * 1000; // 5min

    private static Logger logger = LoggerFactory.getLogger(CachingRestClient.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final long DEFAULT_CONNECTION_TTL_SEC = 60; // infran palomuuri katkoo monta minuuttia makaavat connectionit

    private PoolingClientConnectionManager connectionManager;
    private HttpClient cachingClient;

    private final int timeoutMs;

    public CachingHttpGetClient() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_CONNECTION_TTL_SEC);
    }

    public CachingHttpGetClient(int timeoutMs) {
        this(timeoutMs, DEFAULT_CONNECTION_TTL_SEC);
    }

    public CachingHttpGetClient(int timeoutMs, long connectionTimeToLiveSec) {
        this.timeoutMs = timeoutMs;

        // multithread support + max connections
        connectionManager = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault(), connectionTimeToLiveSec, TimeUnit.MILLISECONDS);
        connectionManager.setDefaultMaxPerRoute(100); // default 2
        connectionManager.setMaxTotal(1000); // default 20

        // init stuff
        final DefaultHttpClient actualClient = new DefaultHttpClient(connectionManager);

        HttpParams httpParams = actualClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeoutMs);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        HttpConnectionParams.setSoKeepalive(httpParams, true); // prevent firewall to reset idle connections?


        org.apache.http.impl.client.cache.CacheConfig cacheConfig = new org.apache.http.impl.client.cache.CacheConfig();
        cacheConfig.setMaxCacheEntries(50 * 1000);
        cacheConfig.setMaxObjectSize(10 * 1024 * 1024); // 10M, eg oppilaitosnumero -koodisto is 7,5M
        cachingClient = new CachingHttpClient(actualClient, cacheConfig);
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
            T t = fromJson(resultType, is);
            return t;
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }

    public InputStream get(String url) throws IOException {
        return get(url,(HttpContext) null);
    }

    public InputStream get(String url, HttpContext context) throws IOException {
        HttpGet request = new HttpGet(url);
        final HttpResponse response = cachingClient.execute(request, context);
        if(response.getStatusLine().getStatusCode() == SC_FORBIDDEN) {
            logAndThrow(request, response, "Access denied error calling REST resource");
        }

        if(response.getStatusLine().getStatusCode() >= SC_INTERNAL_SERVER_ERROR) {
            logAndThrow(request, response, "Internal error calling REST resource");
        }

        if(response.getStatusLine().getStatusCode() >= SC_NOT_FOUND) {
            logAndThrow(request, response, "Not found error calling REST resource");
        }
        return response.getEntity().getContent();
    }

    private void logAndThrow(HttpRequestBase req, HttpResponse response, final String msg) throws HttpException {
        String message = msg + ", url: " + req.getURI() + ", status: " + response.getStatusLine();
        logger.error(message);
        throw new HttpException(req, response, message);
    }

    private <T> T fromJson(Class<? extends T> resultType, InputStream response) throws IOException {
        try {
            return JsonSupport.gson.fromJson(new InputStreamReader(response, UTF8), resultType);
        } catch (JsonSyntaxException e) {
            throw new IOException("failed to parse object from (json) response, type: "+resultType.getSimpleName()+", reason: "+e.getCause()+", response:\n"+response);
        }
    }
}

