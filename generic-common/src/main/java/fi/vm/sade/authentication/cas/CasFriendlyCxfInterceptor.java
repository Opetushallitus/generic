package fi.vm.sade.authentication.cas;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Interceptor for handling CAS redirects and authentication transparently when needed.
 * @author Jouni Stam
 *
 * !!! THREAD SAFETY !!!
 * !!! http://cxf.apache.org/docs/jax-rs-client-api.html !!!
 *
 */
public class CasFriendlyCxfInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

	private static final Logger log = LoggerFactory.getLogger(CasFriendlyCxfInterceptor.class);
	
	public CasFriendlyCxfInterceptor() {
		// Intercept in receive phase
		super(Phase.RECEIVE);
	}

	/**
	 * Invoked on in- and outbound (if interceptor is registered for both). 
	 */
	@Override
	public void handleMessage(Message message) throws Fault {
		boolean inbound = (Boolean)message.get(Message.INBOUND_MESSAGE);
		if(inbound) 
			this.handleInbound(message);
		else
			this.handleOutbound(message);
	}

	/**
	 * Invoked on outbound (request).
	 * @param message
	 * @throws Fault
	 */
	public void handleOutbound(Message message) throws Fault {
		log.debug("Outbound message intercepted.");
		// TODO Get and set necessary tokens if available
		HttpURLConnection conn = resolveConnection(message);
		Authentication auth = getAuthentication();
		// TODO Keep sessions somewhere
		String sessionId = null;
		if(auth != null)
			sessionId = getUserSpecificSessionToken(auth);
		if(sessionId != null)
			conn.addRequestProperty("JSESSIONID", sessionId);
	}
	
	/**
	 * Invoked on inbound (response).
	 * @param message
	 * @throws Fault
	 */
	public void handleInbound(Message message) throws Fault {
		log.debug("Inbound message intercepted.");		
		
		@SuppressWarnings("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
		
		Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
		log.debug("Original response code: " + responseCode);
		
		List<String> locationHeader = headers.get("Location");
		String location = null;
		if(locationHeader != null) 
			location = locationHeader.get(0);
		if(location != null) {
			log.debug("Redirect proposed: " + location);
			try {
				URL url = new URL(location);
		    	String path = url.getPath();
		    	// We are only interested in CAS redirects
		    	if(path.startsWith("/cas/login")) {
					// TODO Currently resends the first request as well, can be optimized to go to /cas/login directly, but
					// that would require additional logic to get the original request from message  
					
					// Follow redirects in a separate CasFriendlyHttpClient request chain
					CasFriendlyHttpClient casClient = new CasFriendlyHttpClient();
		
					// FIXME Use authentication or take service credentials from properties
					String login = "ophadmin";
//			    	String password = "ilonkautta!";
					String password = "Meilt%C3%A4h%C3%A4n%20t%C3%A4m%C3%A4%20k%C3%A4y%3F";
			    	HttpContext context = new BasicHttpContext();
			    	context.setAttribute(CasRedirectStrategy.ATTRIBUTE_LOGIN, login);
			    	context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PASSWORD, password);
			    	
					HttpResponse response = casClient.execute(CasFriendlyHttpClient.createRequest(message), context);
					
					// Set values back to message from response
					fillMessage(message, response);
					
					// Set session ids
					String sessionId = resolveSessionId(response);
					if(sessionId != null) {
						String serviceUrl = (String)context.getAttribute(CasRedirectStrategy.ATTRIBUTE_SERVICE_URL);
						String userName = getAuthentication().getName();
						setSessionIdToCache(serviceUrl, userName, sessionId);
					}
					
				}
			} catch(Exception ex) {
				log.warn("Error while calling for CAS.", ex);
			}
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
	private static void fillMessage(Message message, HttpResponse response) throws IllegalStateException, IOException {
		// Set body
 		Message inMessage = message.getExchange().getInMessage();
		InputStream is = response.getEntity().getContent();
		CachedOutputStream bos = new CachedOutputStream();
		IOUtils.copy(is,bos);
        bos.flush();
        bos.close();
        inMessage.setContent(InputStream.class, bos.getInputStream());
        
        // Set status code
        message.getExchange().put(Message.RESPONSE_CODE, new Integer(response.getStatusLine().getStatusCode()));
        
        // TODO Set headers?
                
	}

	protected void setSessionIdToCache(String serviceUrl, String userName, String sessionId) {
		// FIXME Take sessionId from the cookies
		CasFriendlyCache cache = new CasFriendlyCache();
		cache.setSessionId(serviceUrl, userName, sessionId);
	}

	private static String resolveSessionId(HttpResponse response) {
		Header cookies = response.getFirstHeader("Cookie");
		// FIXME Take it from the response cookies
		return "FIXME";
	}
	
	/**
	 * Invoked on error.
	 */
	@Override
	public void handleFault(Message message) {
		log.debug("Handle fault: " + message);
		super.handleFault((T)message);
	}

	/**
	 * Gets authentication object if available, otherwise returns null.
	 * @return
	 */
	protected static Authentication getAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication.isAuthenticated())
			return authentication;
		else
			return null;
	}
	
	/**
	 * Resolves session token (JSESSIONID) if any available for this user.
	 * @return
	 */
	protected static String getUserSpecificSessionToken(Authentication auth) {
		// TODO Keep user specific session ids somewhere
//		return store.get(auth.getName());
		return null;
	}
	
	/**
	 * Gets the connection from given message.
	 * @param message
	 * @return
	 */
	private static HttpURLConnection resolveConnection(Message message) {
		HttpURLConnection conn = (HttpURLConnection)message.getExchange().getOutMessage().get(HTTPConduit.KEY_HTTP_CONNECTION);
		return conn;
	}
	
}
