package fi.vm.sade.generic.ui.app;

import fi.vm.sade.generic.common.auth.xml.TicketHeader;
import fi.vm.sade.generic.ui.portlet.security.SecurityTicketCallback;
import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * @author Eetu Blomqvist
 */
public class LiferaySecurityTicketCallback implements SecurityTicketCallback {

    @Override
    public TicketHeader getTicketHeader() {
        TicketHeader ticketHeader = new TicketHeader();
        User u = AbstractSadePortletApplication.userThreadLocal.get();
        if (u != null) {
            ticketHeader.username = u.getOid();
            ticketHeader.ticket = u.getTicket();
        }
        return ticketHeader;
    }
}
