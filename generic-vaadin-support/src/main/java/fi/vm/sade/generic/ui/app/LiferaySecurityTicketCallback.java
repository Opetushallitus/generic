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
import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.SecurityTicketCallback;
import fi.vm.sade.generic.ui.portlet.security.User;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import java.util.Enumeration;

/**
 * @author Eetu Blomqvist
 */
// todo: cas todo rethink
public class LiferaySecurityTicketCallback implements SecurityTicketCallback {

    @Override
    public TicketHeader getTicketHeader(SoapMessage message) {
        TicketHeader ticketHeader = new TicketHeader();
        User u = UserFeature.get();
        if (u != null) {
            ticketHeader.username = u.getOid();
//            ticketHeader.ticket = u.getTicket(); cas todo deprecated

            // todo: cas todo temp deprekoi vanhan sydeemin
            try {
                UserLiferayImpl userLiferay = (UserLiferayImpl) u;
//                PortletRequest request = userLiferay.getPortletRequest();
//                PortletSession session = request.getPortletSession();

                // TODO: deprecated: jos ei ole käytetty cassia, luotetaan että on autentikoitu väylässä - poista myöhemmin kun cas kokonaan käytössä!
                if ("true".equals(userLiferay.getGlobalSessionAttribute("USER_authenticatedByCAS"))) {
                    //String proxyTicket = (String) session.getAttribute("USER_proxyTicket_http://localhost:8080/organisaatio-service"/*/j_spring_cas_security_check"*/, PortletSession.APPLICATION_SCOPE);
                    String endpoint = (String) message.get(org.apache.cxf.message.Message.ENDPOINT_ADDRESS);
                    String endpointService = endpoint.substring(endpoint.lastIndexOf("/")+1);
                    Enumeration<String> attributeNames = userLiferay.getGlobalSessionAttributeNames();

                    String proxyTicket = getProxyTicket(userLiferay, endpoint, endpointService, attributeNames);

//                    System.out.println("LiferaySecurityTicketCallback.getTicketHeader, proxyTicket: "+proxyTicket); // TODO: sit jos tää toimii niin sessioon kaikkien serviceiden proxytiketit

                    ticketHeader.casTicket = proxyTicket;
                    //}
                } else {
                    // TODO: cas todo very temp dev ympäristön asetus!
                    ticketHeader.casTicket = "oldDeprecatedSecurity_REMOVE";
                    ticketHeader.username = "admin@oph.fi";
                    String allRoles = "";
                    for (GrantedAuthority authority : UserLiferayImpl.buildMockAuthorities()) {
                        allRoles += authority.getAuthority()+",";
                    }
                    ticketHeader.ticket = allRoles;
                }

            } catch (Exception e) {
                e.printStackTrace(); // todo: errorit ja soutit
            }
        }
        return ticketHeader;
    }

    private String getProxyTicket(UserLiferayImpl session, String endpoint, String endpointService, Enumeration<String> attributeNames) {
        // get correct proxyticket
        String proxyTicket = null;
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            if (name.startsWith("USER_proxyTicket_")) {
                String ticketService = name.substring("USER_proxyTicket_".length());
                /* ei toimi näin koska saatetaan kutsua esbin läpi mutta tiketti on backendiin
                if (casService.startsWith(attrService)) {
                    proxyTicket = (String) session.getAttribute(name, PortletSession.APPLICATION_SCOPE);
                    System.out.println("LiferaySecurityTicketCallback.getTicketHeader, endpoint: "+endpoint+", attr: "+name+", proxyTicket: "+proxyTicket);
                }
                */
                // TODO: ihan vitun ruma, mäppäys järkevämmin, esim esb urlit vois olla aina esim ../cxf/koodisto-service/... jos ohjautuu koodisto-servicelle... vai pitäiskö esbin toimia myös auth proxynä? vai pitäiskö kaikki tiketit laittaa menemään?
//                System.out.println("    endpointSrv: "+endpointService+", ticketSrv: "+ticketService);
                String ticket = (String) session.getGlobalSessionAttribute(name);
                if (false);
                else if (ticketService.contains("koodisto-service") && endpointService.toLowerCase().contains("koodi")) proxyTicket = ticket;
                else if (ticketService.contains("organisaatio-service") && endpointService.toLowerCase().contains("organisaatio")) proxyTicket = ticket;
                else if (ticketService.contains("organisaatio-service") && endpointService.toLowerCase().contains("learningopportunity")) proxyTicket = ticket;
                else if (ticketService.contains("tarjonta-service") && endpointService.toLowerCase().contains("tarjonta")) proxyTicket = ticket;
                else if (ticketService.contains("authentication-service") && endpointService.toLowerCase().contains("authentication")) proxyTicket = ticket;
                else if (ticketService.contains("authentication-service") && endpointService.toLowerCase().contains("access")) proxyTicket = ticket;
                else if (ticketService.contains("authentication-service") && endpointService.toLowerCase().contains("requisition")) proxyTicket = ticket;
                else if (ticketService.contains("authentication-service") && endpointService.toLowerCase().contains("user")) proxyTicket = ticket;
                else if (ticketService.contains("authentication-service") && endpointService.toLowerCase().contains("personal")) proxyTicket = ticket;
                else if (ticketService.contains("oid-service") && endpointService.toLowerCase().contains("oid")) proxyTicket = ticket;
                else if (ticketService.contains("log-service") && endpointService.toLowerCase().contains("log")) proxyTicket = ticket;
            }
        }
//        System.out.println("LiferaySecurityTicketCallback.getTicketHeader, endpoint: "+endpoint+", endpointSrv: "+endpointService+", proxyTicket: "+proxyTicket);
        if (proxyTicket == null) {
            new RuntimeException("WARNING! could not get cas proxyticket, casService: "+endpoint);
        }
        return proxyTicket;
    }
}
