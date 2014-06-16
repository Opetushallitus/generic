package fi.vm.sade.authentication.cas;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.cxf.interceptor.Fault;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.HttpContext;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;

/**
 * Filter to intercept Jersey requests for handling CAS redirects and authentication transparently when needed.
 * Acts like an interceptor (which would require JAX-RS 2.0).
 * @author Jouni Stam
 *
 */
@Provider
public class CasFriendlyJerseyClientFilter extends ClientFilter {

    private static final Logger log = LoggerFactory.getLogger(CasFriendlyJerseyFilter.class);

    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_COOKIE_SEPARATOR = "; ";

    private static final String SPRING_CAS_SUFFIX = "j_spring_cas_security_check";

    @Autowired
    CasFriendlyCache sessionCache;

    private String sessionCookieName = "JSESSIONID";
    private String callerService = "any";
    private String login = null;
    private String password = null;
    private long maxWaitTimeMillis = 3000;
    private boolean sessionRequired = true;

    /**
     * Request filter.
     */
    @Override
    public ClientResponse handle(ClientRequest request) {
        log.debug("Outbound message intercepted.");
        //		HttpURLConnection conn = resolveConnection(message);
        Authentication auth = this.getAuthentication();
        try {
            String targetServiceUrl = resolveTargetServiceUrl(request);
            String sessionId = null;
            String userName = (auth != null)?auth.getName():login;
            sessionId = this.getSessionIdFromCache(callerService, targetServiceUrl, userName);
            if(sessionId == null) {
                // Block multiple requests if necessary, lock if no concurrent running
                this.sessionCache.waitOrFlagForRunningRequest(callerService, targetServiceUrl, userName, this.getMaxWaitTimeMillis(), true);
                // Might be available now
                sessionId = this.getSessionIdFromCache(callerService, targetServiceUrl, userName);
            }
            // Set sessionId if possible before making the request
            if(sessionId != null) 
                injectSessionCookie(request, sessionId);
            else if(this.isSessionRequired()) {
                // Do this proactively only if session is required.

                // Do CAS authentication request (multiple requests)
                this.doCasAuthentication(request);

                // Might be available now
                sessionId = this.getSessionIdFromCache(callerService, targetServiceUrl, userName);

                // Set sessionId if possible before making the request
                if(sessionId != null) 
                    injectSessionCookie(request, sessionId);

            }
        } catch(Exception ex) {
            log.error("Unable process outbound message in interceptor.", ex);
            throw new Fault(ex);
        }
        // Call the next client handler in the filter chain
        ClientResponse resp = getNext().handle(request);

        // Modify the response
        return handleResponse(resp);
    }

    /**
     * Invoked before giving the response back.
     * @param response
     * @return
     */
    private ClientResponse handleResponse(ClientResponse response) {
        return response;
    }

    /**
     * Does CAS authentication procedure and goes back to the original request after that
     * to get the response from original source after authentication.
     * @param message
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    private HttpResponse doCasAuthentication(ClientRequest request) throws Exception {
        // TODO Currently resends the first request as well, can be optimized to go to /cas/login directly,
        // which would require some additional logic to get the original request from message  

        String targetServiceUrl = null;
        String userName = null;

        try {
            // Follow redirects in a separate CasFriendlyHttpClient request chain
            CasFriendlyHttpClient casClient = new CasFriendlyHttpClient();

            Authentication auth = this.getAuthentication();

            HttpContext context = null;

            if(login != null && password != null)
                context = casClient.createHttpContext(login, password, this.sessionCache);
            else if(auth != null && auth.getPrincipal() instanceof AttributePrincipal)
                context = casClient.createHttpContext((AttributePrincipal)auth.getPrincipal(), this.sessionCache);
            else
                return null;

            HttpUriRequest uriRequest = CasFriendlyHttpClient.createRequest(request);
            HttpResponse response = casClient.execute(uriRequest, context);

            targetServiceUrl = (String)context.getAttribute(CasRedirectStrategy.ATTRIBUTE_SERVICE_URL);
            userName = (login != null)?login:auth.getName();

            // Set session ids
            CookieStore cookieStore = (CookieStore)context.getAttribute(ClientContext.COOKIE_STORE);
            String sessionId = resolveSessionId(cookieStore);
            if(sessionId != null) {
                // Set Authentication
                auth.setAuthenticated(true);
                //				SecurityContextHolder.getContext().setAuthentication(auth);
                // Set to cache
                setSessionIdToCache(this.getCallerService(), targetServiceUrl, userName, sessionId);
                log.debug("Session cached: " + sessionCache.getSessionId(this.getCallerService(), targetServiceUrl, userName));
            }

            return response;
        } finally {
            // Release request for someone else
            if(targetServiceUrl != null && userName != null)
                this.sessionCache.releaseRequest(this.getCallerService(), targetServiceUrl, userName);
        }
    }

    /**
     * Recreates message based on response.
     * @param message
     * @param response
     * @return
     * @throws IOException 
     * @throws IllegalStateException 
     */
    private static void fillMessage(ContainerRequest request, ContainerResponse response, HttpResponse httpResponse) throws IllegalStateException, IOException {
        // Set body from final request. Overwrites the original response body.
        //		InputStream is = httpResponse.getEntity().getContent();
        //		CachedOutputStream bos = new CachedOutputStream();
        //		IOUtils.copy(is,bos);
        //        bos.flush();
        //        bos.close();
        response.setEntity(httpResponse.getEntity());

        // Set status code from final request.
        response.setStatus(httpResponse.getStatusLine().getStatusCode());

        // Set headers
        response.getHttpHeaders().clear();
        HeaderIterator iter = httpResponse.headerIterator();
        while(iter.hasNext()) {
            Header one = iter.nextHeader();
            response.getHttpHeaders().add(one.getName(), one.getValue());
        }

    }

    /**
     * Sets session Id to cache.
     * @param callerService
     * @param targetServiceUrl
     * @param userName
     * @param sessionId
     */
    protected void setSessionIdToCache(String callerService, String targetServiceUrl, String userName, String sessionId) {
        sessionCache.setSessionId(callerService, targetServiceUrl, userName, sessionId);
    }

    /**
     * Gets session Id from cache if any available for this user.
     * @return
     */
    protected String getSessionIdFromCache(String callerService, String targetServiceUrl, String userName) {
        return sessionCache.getSessionId(callerService, targetServiceUrl, userName);
    }

    /**
     * Takes the session cookie value from response.
     * @param response
     * @return Returns session Id or null if not set.
     */
    private String resolveSessionId(CookieStore cookieStore) {
        // Get from cookie store
        for(org.apache.http.cookie.Cookie cookie:cookieStore.getCookies()) {
            if(cookie.getName().equals(this.getSessionCookieName()))
                return cookie.getValue();
        }
        return null;
    }

    /**
     * Gets authentication object if available, otherwise returns null.
     * @return
     */
    protected Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated())
            return authentication;
        else
            return null;
    }

    /**
     * Resolves target service URL from message's endpoint address.
     * @param message
     * @return
     * @throws MalformedURLException
     */
    private String resolveTargetServiceUrl(ClientRequest request) throws MalformedURLException {
        String targetUrl = request.getURI().toString();
        URL url = new URL(targetUrl);
        String port = ((url.getPort() > 0)?(":" + url.getPort()):"");
        String[] folders = url.getPath().split("/");
        String path = "/";
        if(folders.length > 0)
            path += folders[1] + "/";
        String finalUrl = url.getProtocol() + "://" + 
                url.getHost() + port + path + SPRING_CAS_SUFFIX;
        return finalUrl.toString();
    }

    /**
     * Injects session cookie to the request.
     * @param request
     * @param id
     */
    private void injectSessionCookie(ClientRequest request, String id) {
        MultivaluedMap<String, Object> headers = request.getHeaders();

        String cookieHeader = headers.getFirst(HEADER_COOKIE).toString();
        List<HttpCookie> cookies = null;
        if(cookieHeader != null)
            cookies = HttpCookie.parse(cookieHeader);
        else
            cookies = new ArrayList<HttpCookie>();
        for(HttpCookie one:cookies) {
            if(this.getSessionCookieName().equals(one.getName())) {
                cookies.remove(one);
                break;
            }	
        }
        cookies.add(new HttpCookie(this.getSessionCookieName(), id));
        cookieHeader = toCookieString(cookies);
        List<Object> cookieHeaders = new ArrayList<Object>();
        cookieHeaders.add(cookieHeader);
        request.getHeaders().put(HEADER_COOKIE, cookieHeaders);
        log.debug("Injected cached session id: " + id);
    }

    private static String toCookieString(List<HttpCookie> cookies) {
        StringBuilder cookieString = new StringBuilder();
        for (HttpCookie httpCookie : cookies) {
            cookieString.append(cookieString.length() > 0 ? HEADER_COOKIE_SEPARATOR : "").append(httpCookie.getName()).append("=").append(httpCookie.getValue());
        }
        return cookieString.toString();
    }

    /**
     * Caller service is the distinctive name of the service. Used to keep sessions service specific if needed.  
     * @return
     */
    public String getCallerService() {
        return callerService;
    }

    public void setCallerService(String callerService) {
        this.callerService = callerService;
    }

    /**
     * Login for authenticate as a service.
     * @return
     */
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Password for authenticate as a service.
     * @return
     */	
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Cookie name for sessionId.
     * @return
     */
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     * Cache for sessions. Should be autowired by default.
     * @return
     */
    public CasFriendlyCache getCache() {
        return sessionCache;
    }

    public void setCache(CasFriendlyCache cache) {
        this.sessionCache = cache;
    }

    /**
     * Maximum wait time for concurrent requests. No blocking if <= 0.
     * @return
     */
    public long getMaxWaitTimeMillis() {
        return maxWaitTimeMillis;
    }

    public void setMaxWaitTimeMillis(long maxWaitTimeMillis) {
        this.maxWaitTimeMillis = maxWaitTimeMillis;
    }

    /**
     * If session is required, it is proactively fetched in the outbound phase already. Otherwise only 
     * fetched in case of /cas/login redirect.
     * @return
     */
    public boolean isSessionRequired() {
        return sessionRequired;
    }

    public void setSessionRequired(boolean sessionRequired) {
        this.sessionRequired = sessionRequired;
    }

}
