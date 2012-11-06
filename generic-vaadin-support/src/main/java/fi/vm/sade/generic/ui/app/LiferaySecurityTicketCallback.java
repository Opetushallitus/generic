/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
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
