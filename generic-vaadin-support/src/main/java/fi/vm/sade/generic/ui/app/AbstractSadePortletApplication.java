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

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Configurable;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.gwt.server.PortletRequestListener;

import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * @author Antti
 * @author Marko Lyly
 */
@Configurable(preConstruction = false)
public abstract class AbstractSadePortletApplication extends AbstractBlackboardSadeApplication implements
        PortletRequestListener, ApplicationContext.TransactionListener {

    @Override
    public synchronized void init() {
        super.init();
    }

    @Override
    public void transactionStart(Application application, Object transactionData) {
        super.transactionStart(application, transactionData);
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        super.transactionEnd(application, transactionData);
    }

    @Override
    public void onRequestStart(PortletRequest portletRequest, PortletResponse portletResponse) {
        User user = new UserLiferayImpl(portletRequest);
        setUser(user);
        setLocale(user.getLang());
    }

    @Override
    public void onRequestEnd(PortletRequest portletRequest, PortletResponse portletResponse) {
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        super.onRequestStart(request, response);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        super.onRequestEnd(request, response);
    }

    @Override
    protected void registerListeners(Blackboard blackboard) {
        // TODO Auto-generated method stub

    }

}
