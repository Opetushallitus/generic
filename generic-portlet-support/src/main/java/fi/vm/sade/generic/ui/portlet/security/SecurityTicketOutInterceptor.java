package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import java.net.HttpURLConnection;

/**
 * Interceptor for adding a security ticket SOAP header into outbound SOAP message.
 * Should be used by ticket-aware clients of services.
 *
 * @author Eetu Blomqvist
 */
// todo: cas todo rethink
public class SecurityTicketOutInterceptor extends AbstractSoapInterceptor {

    SecurityTicketCallback callback;

    public SecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SoapPreProtocolOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        TicketHeader ticketHeader = callback.getTicketHeader(message);
        ((HttpURLConnection)message.get("http.connection")).setRequestProperty("CasSecurityTicket", ticketHeader.casTicket); // todo: cas ticket
        ((HttpURLConnection)message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_username", ticketHeader.username);
        ((HttpURLConnection)message.get("http.connection")).setRequestProperty("oldDeprecatedSecurity_REMOVE_authorities", ticketHeader.ticket);
    }

    public void setCallback(SecurityTicketCallback callback) {
        this.callback = callback;
    }
}
