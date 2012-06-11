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


import com.vaadin.terminal.gwt.server.PortletRequestListener;
import fi.vm.sade.generic.common.I18N;
import org.springframework.beans.factory.annotation.Configurable;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.Locale;

/**
 * @author Antti
 * @author Marko Lyly
 */
@Configurable(preConstruction = false)
public abstract class AbstractSadePortletApplication extends AbstractSadeApplication implements PortletRequestListener {

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

    @Override
    public void onRequestStart(PortletRequest portletRequest, PortletResponse portletResponse) {
        log.info("onRequestStart() - portlet");
        onRequestStart(portletRequest);
    }

    @Override
    public void onRequestEnd(PortletRequest portletRequest, PortletResponse portletResponse) {
        log.info("onRequestEnd() - portlet");
        // empty
    }
}
