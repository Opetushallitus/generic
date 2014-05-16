package fi.vm.sade.authentication.cas;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.spi.container.ContainerRequest;

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
				logHeaders(request);
				
				if(context.getAttribute(CasRedirectStrategy.ATTRIBUTE_ORIGINAL_REQUEST) == null) {
					log.debug("Started with original request: " + request.getRequestLine().getUri());
					
					// TODO Add session Id cookie from cache if available
					CasFriendlyCache cache = (CasFriendlyCache)context.getAttribute(CasRedirectStrategy.ATTRIBUTE_CACHE);
					if(cache != null) {
//						String sessionId = cache.getSessionId("any", targetServiceUrl, userName);
					}
					
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
	 * Creates a CAS client request based on intercepted CXF message.
	 * @param message
	 * @throws IOException 
	 */
	public static HttpUriRequest createRequest(Message message) throws IOException {
		// Original out message (request)
		Message outMessage = message.getExchange().getOutMessage();
		String method = (String)outMessage.get(Message.HTTP_REQUEST_METHOD);
		String url = (String)outMessage.get(Message.ENDPOINT_ADDRESS);
		String encoding = (String)outMessage.get(Message.ENCODING);
		if(StringUtils.isEmpty(encoding))
			encoding = "UTF-8";
		
		// Get headers
		@SuppressWarnings ("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>)outMessage.get(Message.PROTOCOL_HEADERS);
		
		// Get the body of request
		String body = null;
		InputStream is = outMessage.getContent(InputStream.class);
		if(is != null) {
			CachedOutputStream bos = new CachedOutputStream();
			IOUtils.copy(is, bos);
			body = new String(bos.getBytes(), encoding);
		}
		
		// Create request based on method
		HttpUriRequest uriRequest = null;
		if(method.equalsIgnoreCase("POST")) {
			uriRequest = new HttpPost(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		} else if(method.equalsIgnoreCase("GET")) {
			uriRequest = new HttpGet(url);
		} else if(method.equalsIgnoreCase("DELETE")) {
			uriRequest = new HttpDelete(url);
		} else if(method.equalsIgnoreCase("PUT")) {
			uriRequest = new HttpPut(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		}

		// Set headers to request
		for(String one:headers.keySet()) {
			List<String> values = headers.get(one);
			// Just add the first value
			uriRequest.addHeader(one, values.get(0));
		}
		
		return uriRequest;
		
	}
	
	/**
	 * Creates a CAS client request based on intercepted Jersey request.
	 * @param message
	 * @throws IOException 
	 */
	public static HttpUriRequest createRequest(ContainerRequest request) throws IOException {
		// Original out message (request)
		String method = request.getMethod();
		String url = request.getBaseUri().toURL().toString();
		String encoding = request.getHeaderValue("Content-Encoding");
		if(StringUtils.isEmpty(encoding))
			encoding = "UTF-8";
		
		// Get headers
		@SuppressWarnings ("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>)request.getRequestHeaders();
		
		// Get the body of request
		String body = null;
		InputStream is = request.getEntityInputStream();
		if(is != null) {
			CachedOutputStream bos = new CachedOutputStream();
			IOUtils.copy(is, bos);
			body = new String(bos.getBytes(), encoding);
		}
		
		// Create request based on method
		HttpUriRequest uriRequest = null;
		if(method.equalsIgnoreCase("POST")) {
			uriRequest = new HttpPost(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		} else if(method.equalsIgnoreCase("GET")) {
			uriRequest = new HttpGet(url);
		} else if(method.equalsIgnoreCase("DELETE")) {
			uriRequest = new HttpDelete(url);
		} else if(method.equalsIgnoreCase("PUT")) {
			uriRequest = new HttpPut(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		}

		// Set headers to request
		for(String one:headers.keySet()) {
			List<String> values = headers.get(one);
			// Just add the first value
			uriRequest.addHeader(one, values.get(0));
		}
		
		return uriRequest;
		
	}
	
	/**
	 * Creates a CAS client request based on intercepted Jersey request.
	 * @param message
	 * @throws IOException 
	 */
	public static HttpUriRequest createRequest(ClientRequest request) throws IOException {
		// Original out message (request)
		String method = request.getMethod();
		String url = request.getURI().toString();
		String encoding = (String)request.getHeaders().getFirst("Content-Encoding");
		if(StringUtils.isEmpty(encoding))
			encoding = "UTF-8";
		
		// Get headers
		@SuppressWarnings ("unchecked")
		Map<String, List<Object>> headers = (Map<String, List<Object>>)request.getHeaders();
		
		// Get the body of request
		// FIXME Not sure how to get body yet
		String body = null;
//		InputStream is = request.getEntity().getEntityInputStream();
//		if(is != null) {
//			CachedOutputStream bos = new CachedOutputStream();
//			IOUtils.copy(is, bos);
//			body = new String(bos.getBytes(), encoding);
//		}
		
		// Create request based on method
		HttpUriRequest uriRequest = null;
		if(method.equalsIgnoreCase("POST")) {
			uriRequest = new HttpPost(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		} else if(method.equalsIgnoreCase("GET")) {
			uriRequest = new HttpGet(url);
		} else if(method.equalsIgnoreCase("DELETE")) {
			uriRequest = new HttpDelete(url);
		} else if(method.equalsIgnoreCase("PUT")) {
			uriRequest = new HttpPut(url);
			if(body != null)
				((HttpPost)uriRequest).setEntity(new StringEntity(body));
		}

		// Set headers to request
		for(String one:headers.keySet()) {
			String value = (String)request.getHeaders().getFirst(one);
			// Just add the first value
			if(value != null)
				uriRequest.addHeader(one, value);
		}
		
		return uriRequest;
		
	}
	
	/**
	 * Creates a context with principal set.
	 * @param principal
	 * @return
	 */
	public HttpContext createHttpContext(AttributePrincipal principal, CasFriendlyCache cache) {
		HttpContext context = super.createHttpContext();
		// Add principal attribute
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PRINCIPAL, principal);
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_CACHE, cache);
		return context;
	}

	/**
	 * Creates a context with principal mock from given login and password.
	 * @param principal
	 * @return
	 */
	public HttpContext createHttpContext(String login, String password, CasFriendlyCache cache) {
		HttpContext context = super.createHttpContext();
		// Add credentials
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_LOGIN, login);
		context.setAttribute(CasRedirectStrategy.ATTRIBUTE_PASSWORD, password);
		return context;
	}

	private static void logHeaders(HttpRequest request) {
		log.debug("Request headers: ");
		for(Header one:request.getAllHeaders()) {
			log.debug(one.getName() + ": " + one.getValue());
		}
	}
}
