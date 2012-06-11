/*
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

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * TODO This could be moved as common base class.
 *
 * @author Jukka Raanamo
 * @author Marko Lyly
 */
public abstract class AbstractSadeApplication extends Application implements TransactionListener, HttpServletRequestListener {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * System default locale - defined to be "fi".
     */
    public static final String DEFAULT_LOCALE = "fi";

    /**
     * Default theme is "sade"
     */
    public static final String THEME_NAME = "sade";

    private Blackboard blackboardInstance = new Blackboard();

    protected Locale sessionLocale = new Locale(DEFAULT_LOCALE);

    /**
     * When overriding this method, remember to call super as the first thing.
     */
    @Override
    public synchronized void init() {
        //Init blackboard event bus
        registerListeners(blackboardInstance);

        // set blackboard to threadlocal - NOTE! tehdään transactionStartissa joten tästä pois
        //setBlackboard(blackboardInstance);

        log.info("init(), current locale: {}, reset to session locale: {}" , I18N.getLocale(), sessionLocale);
        setLocale(sessionLocale);

        // At every "transaction" start set the threadlocal blackboard instance
        getContext().addTransactionListener(this);
    }

    /**
     * Invoked at init to register event listeners and events with given event bus.
     *
     * @param blackboard
     */
    protected abstract void registerListeners(Blackboard blackboard);

    /*
     * override Application.setLocale to set locale also to I18N and to the Spring framework locale context holder.
     */
    @Override
    public void setLocale(Locale locale) {
        log.debug("setLocale({})", locale);
        I18N.setLocale(locale);
        LocaleContextHolder.setLocale(locale);
        super.setLocale(locale);
    }

    /*
     * Implement TransactionListener interface
     */
    @Override
    public void transactionStart(Application application, Object transactionData) {
        if (application == this) {
            BlackboardContext.setBlackboard(blackboardInstance);
        }
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        if (application == this) {
            BlackboardContext.setBlackboard(null);
        }
    }

    /*
     * Implement HttpServletRequestListener interface
     */
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        onRequestStart(request);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        // empty
    }

    /**
     * Get "lang" request parameter (if present) and set current locale for the application and
     * I18N service.
     *
     * @param request
     */
    protected void onRequestStart(Object request) {
        // TODO: testejä varten - HUOM! lang-parametrin ohessa pitää antaa myös 'restartApplication', muuten locale ei vaihdu oikein
        String langParam = getParameter(request, "lang");
        if (langParam != null) {
            sessionLocale = new Locale(langParam);
        }

        setLocale(sessionLocale);
        log.debug("onRequestStart(): ", requestInfo(request));
    }

    /**
     * Get ThreadLocal Blackboard instance
     * @return
     */
    public static Blackboard getBlackboard() {
        return BlackboardContext.getBlackboard();
    }

    /**
     * Gets parameter value from HttpServletRequest.
     *
     * @param req
     * @param name
     * @return
     */
    protected String getParameter(Object req, String name) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getParameter(name);
    }

    /**
     * Create string information from the given (http) request (for debugging purposes).
     *
     * @param req
     * @return
     */
    protected String requestInfo(Object req) {
        HttpServletRequest request = (HttpServletRequest) req;

        StringBuilder sb = new StringBuilder();
        sb.append(", sessionLocale: ");
        sb.append(sessionLocale.toString());
        sb.append(", langParam: ");
        sb.append(request.getParameter("lang"));
        sb.append(", url: ");
        sb.append(request.getRequestURL());
        sb.append(", i18n.locale: ");
        sb.append(I18N.getLocale());
        sb.append(", default locale: ");
        sb.append(Locale.getDefault());

        return sb.toString();
    }
}
