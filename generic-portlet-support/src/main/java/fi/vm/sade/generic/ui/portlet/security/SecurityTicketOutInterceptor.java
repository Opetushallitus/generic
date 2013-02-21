package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.generic.common.auth.xml.ElementNames;
import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.phase.Phase;
import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.net.HttpURLConnection;

/**
 * Interceptor for adding a security ticket SOAP header into outbound SOAP message.
 * Should be used by ticket-aware clients of services.
 *
 * @author Eetu Blomqvist
 */
public class SecurityTicketOutInterceptor extends AbstractSoapInterceptor {

    SecurityTicketCallback callback;

    public SecurityTicketOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
        getAfter().add(SoapPreProtocolOutInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {

        TicketHeader ticketHeader = callback.getTicketHeader(message);

        try {
            SoapHeader header = new SoapHeader(ElementNames.SECURITY_TICKET_QNAME, ticketHeader,
                    new JAXBDataBinding(TicketHeader.class));
            message.getHeaders().add(header);

            ((HttpURLConnection)message.get("http.connection")).setRequestProperty("CasSecurityTicket", ticketHeader.casTicket); // todo: cas ticket
            if ("oldDeprecatedSecurity_REMOVE".equals(ticketHeader.casTicket)) {
                if (message.get(SoapMessage.QUERY_STRING) != null) {
                    throw new RuntimeException("soapmessage already has querystring");
                } else {
                    message.put(SoapMessage.QUERY_STRING, "oldDeprecatedSecurity_REMOVE=true");
                }
            }

        } catch (JAXBException e) {
            throw new Fault(e, new QName(ElementNames.SADE_URI, ElementNames.AUTHENTICATION_FAILED));
        }
    }

    public void setCallback(SecurityTicketCallback callback) {
        this.callback = callback;
    }
}
