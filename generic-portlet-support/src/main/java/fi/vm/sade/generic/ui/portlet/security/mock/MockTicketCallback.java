package fi.vm.sade.generic.ui.portlet.security.mock;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import fi.vm.sade.generic.ui.portlet.security.SecurityTicketCallback;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * Mock implementation of ticket callback.
 *
 * @author Eetu Blomqvist
 */
public class MockTicketCallback implements SecurityTicketCallback {

    @Override
    public TicketHeader getTicketHeader(SoapMessage message) {
        TicketHeader ticketHeader = new TicketHeader();
        ticketHeader.username = "1.2.246.562.24.00000000001";
        ticketHeader.ticket = "lc8925kjgi12nfd91";
        return ticketHeader;
    }
}
