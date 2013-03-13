package fi.vm.sade.generic.ui.portlet.security;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * This interface is used to retrieve the header element for security ticket by
 * the interceptor attached to CXF.
 */
public interface SecurityTicketCallback {

    /**
     * Returns the header element containing username and ticket value.
     *
     * @return ticket header
     * @param message
     */
    TicketHeader getTicketHeader(SoapMessage message);
}
