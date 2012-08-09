/**
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

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.springframework.beans.factory.annotation.Configurable;

import com.vaadin.terminal.gwt.server.PortletRequestListener;

import fi.vm.sade.generic.common.I18N;

/**
 * @author Antti
 * @author Marko Lyly
 */
@Configurable(preConstruction = false)
public abstract class AbstractSadePortletApplication extends AbstractBlackboardSadeApplication implements
        PortletRequestListener {

    private static ThreadLocal<PortletRequest> threadLocalPortletRequest = new ThreadLocal<PortletRequest>();

    /*
     * Override to get params from portlet request
     */
    @Override
    protected String getParameter(Object req, String name) {
        PortletRequest request = (PortletRequest) req;
        return request.getParameter(name);
    }

    /*
     * Override from the base class (has to use portlet request)
     */
    @Override
    protected String requestInfo(Object req) {
        PortletRequest request = (PortletRequest) req;

        StringBuilder sb = new StringBuilder();
        sb.append(", sessionLocale: ");
        sb.append(sessionLocale.toString());
        sb.append(", langParam: ");
        sb.append(request.getPublicParameterMap().get("lang"));
        sb.append(", i18n.locale: ");
        sb.append(I18N.getLocale());
        sb.append(", default locale: ");
        sb.append(Locale.getDefault());

        return sb.toString();
    }

    /**
     * Pulls Liferay dependencies, enable if needed
     * 
     * @return
     */
    // @SuppressWarnings("unchecked")
    // protected List<AccessRight> getRawAccessRights() {
    // HttpServletRequest httpServletRequest =
    // PortalUtil.getHttpServletRequest(threadLocalPortletRequest.get());
    // Object o =
    // httpServletRequest.getSession().getAttribute(SecuritySessionAttributes.AUTHENTICATION_DATA);
    //
    // List<AccessRight> list = new ArrayList<AccessRight>();
    // if (o != null && o instanceof List) {
    // try {
    // list = (List<AccessRight>) o;
    // return list;
    // } catch (ClassCastException e) {
    // log.warn("Failed to get "
    // + SecuritySessionAttributes.AUTHENTICATION_DATA
    // +
    // " Attribute from session. Session contained something else than expected. Expected List<AccessRight> got: ["
    // + o + "]");
    // }
    // }
    // return list;
    // }

    protected static boolean isUserInRole(String role) {
        return threadLocalPortletRequest.get().isUserInRole(role);
    }

    @Override
    public void onRequestStart(PortletRequest portletRequest, PortletResponse portletResponse) {
        log.info("onRequestStart() - portlet");
        threadLocalPortletRequest.set(portletRequest);
        onRequestStart(portletRequest);
    }

    @Override
    public void onRequestEnd(PortletRequest portletRequest, PortletResponse portletResponse) {
        log.info("onRequestEnd() - portlet");
        // empty
    }
}
