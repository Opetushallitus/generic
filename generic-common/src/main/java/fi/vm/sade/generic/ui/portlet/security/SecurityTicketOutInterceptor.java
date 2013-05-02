package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.HttpURLConnection;

/**
 * Interceptor for adding a security ticket SOAP header into outbound SOAP message.
 * Should be used by ticket-aware clients of services.
 *
 * @author Eetu Blomqvist
 */
public class SecurityTicketOutInterceptor extends AbstractSoapInterceptor {

    private final static Logger log = LoggerFactory.getLogger(SecurityTicketOutInterceptor.class);

    public SecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SoapPreProtocolOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof CasAuthenticationToken) {
            String endpointAddress = (String) message.get(Message.ENDPOINT_ADDRESS) + "/j_spring_cas_security_check";
            log.info("CAS Endpoint: " + endpointAddress);
            CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) authentication;
            String proxyTicket = casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(endpointAddress);
            log.info("CAS Proxy ticket: " + proxyTicket);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", proxyTicket);
            return;
        }

        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", authentication.getName());
        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
        log.warn("Could not attach security ticket to SOAP message from authentication " + authentication + ".");
    }

}
