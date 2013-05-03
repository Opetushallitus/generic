package fi.vm.sade.generic.ui.portlet.security;

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
            String casTargetService = getCasTargetService((String) message.get(Message.ENDPOINT_ADDRESS));
            log.info("CAS Endpoint: " + casTargetService);
            CasAuthenticationToken casAuthenticationToken = (CasAuthenticationToken) authentication;
            String proxyTicket = casAuthenticationToken.getAssertion().getPrincipal().getProxyTicketFor(casTargetService);
            log.info("CAS Proxy ticket: " + proxyTicket);
            ((HttpURLConnection) message.get("http.connection")).setRequestProperty("CasSecurityTicket", proxyTicket);
            return;
        }

        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", authentication.getName());
        //((HttpURLConnection) message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
        log.warn("Could not attach security ticket to SOAP message from authentication " + authentication + ".");
    }

    /**
     * Get cas service from url string, get string before 4th '/' char.
     * For example:
     *
     * https://asd.asd.asd:8080/backend-service/asd/qwe/qwe2.foo?bar=asd
     * --->
     * https://asd.asd.asd:8080/backend-service
     */
    private static String getCasTargetService(String url) {
        return url.replaceAll("(.*?//.*?/.*?)/.*", "$1");
    }

}
