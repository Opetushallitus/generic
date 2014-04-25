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
 * CasRedirectStrategy is where all the magic happens.
 * @author Jouni Stam
 *
 */
public class CasFriendlyHttpClient extends DefaultHttpClient {

	private static final Logger log = LoggerFactory.getLogger(CasFriendlyHttpClient.class);
	public static CasRedirectStrategy casRedirectStrategy = new CasRedirectStrategy();
	
	/**
	 * Constructor sets CasRedirectStategy and adds request interceptor which sets
	 * CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST and CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST_PARAMS
	 * attributes to the HttpContext. 
	 */
	public CasFriendlyHttpClient() {
		
		// Let the super constructor do its work first
		super();
		
		// Set redirect strategy
		this.setRedirectStrategy(casRedirectStrategy);
		
		// Adds an interceptor
		this.addRequestInterceptor(new HttpRequestInterceptor() {

			/**
			 * Takes the first request as original request and stores it later use (after login).
			 */
			@Override
			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				if(context.getAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST) == null) {
					log.debug("Started with original request: " + request.getRequestLine().getUri());
					context.setAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST, request);
					context.setAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST_PARAMS, request.getParams());
				}
			}
		});

		// Not really necessary, only for logging purposes
		this.addResponseInterceptor(new HttpResponseInterceptor() {
			@Override
			public void process(HttpResponse response, HttpContext context)
					throws HttpException, IOException {
				log.debug("Response: " + response.getStatusLine().getStatusCode());
			}
		});
	}

	/**
	 * Helper method to set given principal to given Http context. AttributePrincipal
	 * is used to get the proxy ticket when needed.
	 * @param context
	 * @param principal
	 */
	public static void setAttributePrincipal(HttpContext context, AttributePrincipal principal) {
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PRINCIPAL, principal);
	}
	
}
