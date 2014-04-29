package fi.vm.sade.authentication.cas;

import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor for handling CAS redirects and authentication transparently when needed.
 * @author Jouni Stam
 *
 */
public class CasFriendlyCxfInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {

	private static final Logger log = LoggerFactory.getLogger(CasFriendlyCxfInterceptor.class);
	
	private String myVal = "test";
	
	public CasFriendlyCxfInterceptor() {
		// Intercept in pre protocol phase
		super(Phase.PRE_PROTOCOL);
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
		// TODO Get and set necessary tokens if available
		log.debug("Outbound message: " + message.getId());
	}
	
	/**
	 * Invoked on inbound (response).
	 * @param message
	 * @throws Fault
	 */
	public void handleInbound(Message message) throws Fault {
		@SuppressWarnings("unchecked")
		Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
		log.debug("Inbound message: " + message.getId());
		Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
		log.debug("Response code: " + responseCode);
		List<String> locationHeader = headers.get("Location");
		String location = null;
		if(locationHeader != null) {
			location = locationHeader.get(0);
		}
		if(location != null) {
			// TODO Follow redirects in a separate CasFriendlyHttpClient request chain
			// TODO Does CAS authentication if needed
			
			// TODO After final results set values back to message
			log.debug("Redirect proposed: " + location);
		}
	}
	
	/**
	 * Invoked on error.
	 */
	@Override
	public void handleFault(Message message) {
		log.debug("Handle fault: " + message);
		super.handleFault((T)message);
	}

}
