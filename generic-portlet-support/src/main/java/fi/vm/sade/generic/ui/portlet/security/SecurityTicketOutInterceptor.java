package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.generic.common.auth.xml.ElementNames;
import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.phase.Phase;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

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

        TicketHeader ticketHeader = callback.getTicketHeader();

        try {
            SoapHeader header = new SoapHeader(ElementNames.SECURITY_TICKET_QNAME, ticketHeader,
                    new JAXBDataBinding(TicketHeader.class));
            message.getHeaders().add(header);
        } catch (JAXBException e) {
            throw new Fault(e, new QName(ElementNames.SADE_URI, ElementNames.AUTHENTICATION_FAILED));
        }
    }

    public void setCallback(SecurityTicketCallback callback) {
        this.callback = callback;
    }
}
