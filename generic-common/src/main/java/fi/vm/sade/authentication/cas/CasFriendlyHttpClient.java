package fi.vm.sade.authentication.cas;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultHttpClient enhanced with CAS specific CasRedirectStrategy and
 * HttpRequestInterceptor (to catch and store the original request and params). 
 * @author Jouni Stam
 *
 */
public class CasFriendlyHttpClient extends DefaultHttpClient {

	private static final Logger log = LoggerFactory.getLogger(CasFriendlyHttpClient.class);
	public static CasRedirectStrategy casRedirectStrategy = new CasRedirectStrategy();
	
	private AttributePrincipal principal = null;
	
	private String proxyTicket = null;
	
	public CasFriendlyHttpClient() {
		this.setRedirectStrategy(casRedirectStrategy);
		
		// Adds an interceptor
		this.addRequestInterceptor(new HttpRequestInterceptor() {

			/**
			 * Takes the first request as original request and stores it later use (after login).
			 */
			@Override
			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				log.debug("REQUESTING: " + request.getRequestLine().getUri());
				if(context.getAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST) == null) {
					log.debug("Setting the original request: " + request.getRequestLine().getUri());
					if(principal != null)
						context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PRINCIPAL, principal);
					if(proxyTicket != null)
						context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PROXYTICKET, proxyTicket);
					context.setAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST, request);
					context.setAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST_PARAMS, request.getParams());
				}
			}
		});

		// Not really necessary
		this.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context)
					throws HttpException, IOException {
				log.debug("RESPONSE: " + response.getStatusLine().getStatusCode());
			}
		});
	}

	/**
	 * Sets the principal if available. Can be taken from Spring context or request.
	 * @param principal
	 */
	public void setAttributePrincipal(AttributePrincipal principal) {
		this.principal = principal;
	}
	
	public void setProxyTicket(String proxyTicket) {
		this.proxyTicket = proxyTicket;
	}
	
}
