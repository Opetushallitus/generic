package fi.vm.sade.generic.auth;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import fi.vm.sade.generic.ui.portlet.security.SecurityTicketCallback;

/**
 * @author Eetu Blomqvist
 */
public class LiferaySecurityTicketCallback implements SecurityTicketCallback {

    @Override
    public TicketHeader getTicketHeader() {

        // TODO implement properly
        TicketHeader ticketHeader = new TicketHeader();
        ticketHeader.username = "1.2.246.562.24.27470134097";
        ticketHeader.ticket = "1.2.246.562.24.27470134097";
        return ticketHeader;
    }
}
