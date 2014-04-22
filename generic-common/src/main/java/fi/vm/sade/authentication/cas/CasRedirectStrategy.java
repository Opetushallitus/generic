package fi.vm.sade.authentication.cas;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.EntityEnclosingRequestWrapper;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAS specific redirecting strategy to walk through CAS login transparently.
 * TGT = Ticket Granting Ticket
 * ST = Service Ticket (one timer)
 * PGT = Proxy Granting Ticket
 * PT = Proxy Ticket
 * SESSION = Service specific session (e.g. JSESSIONID) 
 * @author Jouni Stam
 *
 */
@ThreadSafe
public class CasRedirectStrategy implements RedirectStrategy {

	private static final Logger log = LoggerFactory.getLogger(CasRedirectStrategy.class);

	public static final String CAS_TICKET_URL = "/cas/v1/tickets";
	
	public static final String ATTRIBUTE_PRINCIPAL = "principal";
	public static final String ATTRIBUTE_PROXYTICKET = "proxyTicket";
	public static final String ATTRIBUTE_ORIGINAL_REQUEST = "originalRequest";
	public static final String ATTRIBUTE_ORIGINAL_REQUEST_PARAMS = "originalRequestParams";
	public static final String ATTRIBUTE_SERVICE_URL = "serviceUrl";
	public static final String ATTRIBUTE_CAS_REQUEST_STATE = "casRequestState";
	public static final String ATTRIBUTE_CAS_SERVICE_TICKET = "casServicetTicket";

	// First state when redirected to request for TGT
	public static final String CAS_REQUEST_STATE_TGT = "TGT";
	// Second state when redirected to request for ST with TGT
	public static final String CAS_REQUEST_STATE_ST = "ST";
	// Final CAS state when ready to request for SESSION with ST
	public static final String CAS_REQUEST_STATE_SESSION = "SESSION";
	
	/**
	 * Takes care of CAS redirects transparent to the actual request.
	 * Only comes here is isRedirect() is true.
	 */
	@Override
	public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response,
			HttpContext context) throws ProtocolException {
		// Get the redirect URL from location header or use service URL (when CAS state ST)
		String location = null;
		Header redirectLocation = response.getFirstHeader("Location");
		if(redirectLocation != null) {
			location = redirectLocation.getValue();
		} else {
			// Use service URL
			location = (String)context.getAttribute(ATTRIBUTE_SERVICE_URL);
		}
		log.debug("REDIRECT TO: " + location);
		try {
			// Must have location for redirect, otherwise okay to fail
			URL url = new URL(location);
	    	String path = url.getPath();
	    	if(path.startsWith("/cas/login")) {
	        	// Case redirect to /cas/login ("user" must authenticate)
	    		String service = resolveService(location);
	    		
	    		if(service != null)
	    			context.setAttribute(ATTRIBUTE_SERVICE_URL, service);

	    		// Request for TGT
	    		return createTGTRequest(request, response, context, url);

	    	} else if(path.startsWith("/cas/v1/tickets")) {
	    		
    			// Request for service ticket
	    		return createSTRequest(request, response, context, location);
	    		
	    	} else if(context.getAttribute(ATTRIBUTE_CAS_SERVICE_TICKET) != null) {
	    		
	    		// We have service ticket and continue to get session 
	    		return createSessionRequest(request, response, context);
	    		
	    	} else if(CasRedirectStrategy.CAS_REQUEST_STATE_SESSION.equals(context.getAttribute(ATTRIBUTE_CAS_REQUEST_STATE))) {
	    		
	    		// Continue with the original request
	    		return createOriginalRequest(request, response, context);
	    		
	    	} else {
	    		// Not interesting, continue as any other redirect
	    	}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new HttpGet(location);
	}

	/**
	 * Response is considered to be redirect if Location header is given
	 * or (CAS_TICKET_REQUEST && 200).
	 */
	@Override
	public boolean isRedirected(HttpRequest request, HttpResponse response,
			HttpContext context) throws ProtocolException {
		// Continue with original request after ST 
		if(CasRedirectStrategy.CAS_REQUEST_STATE_ST.equals(context.getAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE)) &&
				response.getStatusLine().getStatusCode() == 200) {
			// We are redirecting only because of CAS process (response code is 200) not 301
			try {
				// Read ST from response body
				String serviceTicket = EntityUtils.toString(response.getEntity());
				log.debug("Service ticket is: " + serviceTicket);
				// Reset
				context.removeAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE);
				// Continue back to service with serviceTicket
				context.setAttribute(ATTRIBUTE_CAS_SERVICE_TICKET, serviceTicket);
				// Redirecting
				return true;
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.warn("CAS redirecting strategy confused. Cancelling further redirects.");
			return false;
		} else {
			// Otherwise only interested in responses with location header
			Header redirectLocation = response.getFirstHeader("Location");
	        
	        if (redirectLocation != null) {
	    		// Continue with redirect
	    		return true;
	        } else
	        	return false;
		}
	}

	/**
	 * Creates TGT request to /cas/v1/tickets.
	 * @param request
	 * @param response
	 * @param context
	 * @param locationUrl
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	public static HttpUriRequest createTGTRequest(HttpRequest request, HttpResponse response,
			HttpContext context, URL locationUrl) throws UnsupportedEncodingException {

		String service = (String)context.getAttribute(ATTRIBUTE_SERVICE_URL);
		
		// TODO Figure out if PGT is already available and use that
		String casPgt = resolveProxyTicket(context, service);
		log.debug("PGT: " + casPgt);
		
		String suffix = CAS_TICKET_URL;
		if(casPgt != null)
			suffix = "/cas/proxy";
		
		String url = locationUrl.getProtocol() + "://" + 
				locationUrl.getHost() + ":" + locationUrl.getPort() + suffix;
		HttpPost casRequest = new HttpPost(url);

		// Set state attribute
		log.debug("Setting CAS state to: " + CasRedirectStrategy.CAS_REQUEST_STATE_TGT);
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE, CasRedirectStrategy.CAS_REQUEST_STATE_TGT);
		
		ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
		if(casPgt == null) {
			// Login with service's own credentials
			// FIXME Fix to get the credentials from properties!!!
			postParameters.add(new BasicNameValuePair("service", service));
		    postParameters.add(new BasicNameValuePair("username", "ophadmin"));
		    postParameters.add(new BasicNameValuePair("password", "ilonkautta!"));
		} else {
			// Use PGT
//			service = "https://localhost:8443/organisaatio-ui/j_spring_cas_security_check";
			postParameters.add(new BasicNameValuePair("targetService", service));
			postParameters.add(new BasicNameValuePair("pgt", casPgt));
		}
		casRequest.setEntity(new UrlEncodedFormEntity(postParameters));
		
		return casRequest;
	}

	/**
	 * Creates ST request back to /cas/v1/tickets with service parameter and TGT.
	 * @param request
	 * @param response
	 * @param context
	 * @param locationUrl
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	public static HttpUriRequest createSTRequest(HttpRequest request, HttpResponse response,
			HttpContext context, String locationHeader) throws UnsupportedEncodingException {
		String service = (String)context.getAttribute(ATTRIBUTE_SERVICE_URL);
		HttpPost casRequest = new HttpPost(locationHeader);
		
		log.debug("Setting CAS state to: " + CasRedirectStrategy.CAS_REQUEST_STATE_ST);
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE, CasRedirectStrategy.CAS_REQUEST_STATE_ST);
		
		ArrayList<BasicNameValuePair> postParameters = new ArrayList<BasicNameValuePair>();
	    postParameters.add(new BasicNameValuePair("service", service));
		casRequest.setEntity(new UrlEncodedFormEntity(postParameters));
		return casRequest;
	}
	
	/**
	 * Creates a request to service with ST to get a session.
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static HttpUriRequest createSessionRequest(HttpRequest request, HttpResponse response,
			HttpContext context) {
		log.debug("Setting CAS state to: " + CasRedirectStrategy.CAS_REQUEST_STATE_SESSION);
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE, CasRedirectStrategy.CAS_REQUEST_STATE_SESSION);
		
		String serviceUrl = (String)context.getAttribute(ATTRIBUTE_SERVICE_URL);
		String serviceTicket = (String)context.getAttribute(ATTRIBUTE_CAS_SERVICE_TICKET);
		// Reset
		context.removeAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_SERVICE_TICKET);
		// Create URL
		if(serviceUrl.contains("?"))
			serviceUrl = serviceUrl + "&ticket=" + serviceTicket;
		else
			serviceUrl = serviceUrl + "?ticket=" + serviceTicket;

		HttpGet sessionRequest = new HttpGet(serviceUrl);
		
		return sessionRequest;
	}
	
	/**
	 * Recreates the original request. Required for POST/PUT especially to keep the original
	 * method and parameters in place.
	 * @param request
	 * @param response
	 * @param context
	 * @return
	 * @throws UnsupportedEncodingException 
	 * @throws Exception
	 */
	public static HttpUriRequest createOriginalRequest(HttpRequest request, HttpResponse response,
			HttpContext context) throws UnsupportedEncodingException {
		log.debug("CAS process done. Continuing with the original request.");
		context.removeAttribute(CasRedirectStrategy.ATTRIBUTE_CAS_REQUEST_STATE);
		
		HttpRequest origRequest = (HttpRequest)context.getAttribute(ATTRIBUTE_ORIGINAL_REQUEST);
		String url = resolveUrl(origRequest);
		HttpUriRequest uriRequest = null;
		if(origRequest.getRequestLine().getMethod().contains("POST")) {
			uriRequest = new HttpPost(url);
			EntityEnclosingRequestWrapper origPost = (EntityEnclosingRequestWrapper)origRequest;
			((HttpPost)uriRequest).setEntity(origPost.getEntity());
		} else if(origRequest.getRequestLine().getMethod().contains("GET")) {
			uriRequest = new HttpGet(url);
		} else if(origRequest.getRequestLine().getMethod().contains("DELETE")) {
			uriRequest = new HttpDelete(url);
		} else if(origRequest.getRequestLine().getMethod().contains("PUT")) {
			uriRequest = new HttpPut(url);
			EntityEnclosingRequestWrapper origPut = (EntityEnclosingRequestWrapper)origRequest;
			((HttpPut)uriRequest).setEntity(origPut.getEntity());
		}
		return uriRequest;
	}

	/**
	 * Resolves service from Location header from CAS request.
	 * @param locationHeader
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String resolveService(String locationHeader) throws UnsupportedEncodingException {
		// Set if possible
		if(locationHeader.contains("service=")) {
			String service = StringUtils.substringAfter(locationHeader, "service=");
			service = URLDecoder.decode(service,"UTF-8");
			return service;
		} else
			return null;
	}

	/**
	 * Resolves URL from request.
	 * @param request
	 * @return
	 */
	private static String resolveUrl(HttpRequest request) {
		String protocol = "https";
		String url = protocol + 
				"://" + request.getFirstHeader("Host").getValue() +
				request.getRequestLine().getUri();
		return url;
	}

	/**
	 * Resolves proxy ticket from context.
	 * @param context
	 * @return
	 */
	private static String resolveProxyTicket(HttpContext context, String service) {
		String proxyTicket = 
				(String)context.getAttribute(CasRedirectStrategy.ATTRIBUTE_PROXYTICKET);
		if(proxyTicket != null)
			return proxyTicket;
		AttributePrincipal principal = 
				(AttributePrincipal)context.getAttribute(CasRedirectStrategy.ATTRIBUTE_PRINCIPAL);
		if(principal != null) {
			return principal.getProxyTicketFor(service);
		} else
			return null;
	}
}
