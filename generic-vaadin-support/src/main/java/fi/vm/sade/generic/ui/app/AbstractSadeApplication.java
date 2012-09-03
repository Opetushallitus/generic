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

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

import fi.vm.sade.generic.common.I18N;

/**
 * Super class for sade vaadin based vaadin applications, handles locale.
 * 
 * @author Jukka Raanamo
 * @author Marko Lyly
 */
public abstract class AbstractSadeApplication extends Application implements HttpServletRequestListener {

    private static final long serialVersionUID = 1L;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * System default locale - defined to be "fi".
     */
    public static final String DEFAULT_LOCALE = "fi_FI";

    // protected Locale sessionLocale = new Locale(DEFAULT_LOCALE);

    /**
     * When overriding this method, remember to call super as the first thing.
     */
    @Override
    public void init() {
        setErrorHandler(this);
        setTheme();
    }

    protected void setTheme() {
        this.setTheme("oph");
    }

    /*
     * override Application.setLocale to set locale also to I18N and to the
     * Spring framework locale context holder.
     */
    @Override
    public void setLocale(Locale locale) {
        log.debug("setLocale({})", locale);
        if (locale == null) {
            locale = new Locale(DEFAULT_LOCALE);
            log.debug("locale was null, defaulting({})", locale);
        }
        I18N.setLocale(locale);
        LocaleContextHolder.setLocale(locale);
        super.setLocale(locale);
    }

    private void setLang(String lang) {
        if (StringUtils.isNotBlank(lang)) {
            Locale locale = new Locale(lang);
            setLocale(locale);
        }
    }

    /**
     * shows modal error dialog, for testers, disable for production
     * 
     * @param t
     */
    protected void showStackTrace(Throwable t) {
        if (getMainWindow() != null) {
            final Window dialog = new Window("Exception occurred");
            Label l = new Label(t.toString());
            dialog.addComponent(l);
            dialog.setModal(true);
            getMainWindow().addWindow(dialog);
        }
    }

    public void terminalError(ErrorEvent event) {
        super.terminalError(event);
        if (log.isDebugEnabled()) {
            this.showStackTrace(event.getThrowable());
        }
    }

    /*
     * Implement HttpServletRequestListener interface
     */
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        // NO SUPER
        setUser(new UserLiferayImpl(request));
        setLang(getParameter(request, "lang"));
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        // NO SUPER
        // empty
    }

    /**
     * Gets parameter value from HttpServletRequest.
     * 
     * @param req
     * @param name
     * @return
     */
    private String getParameter(Object req, String name) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getParameter(name);
    }

    /**
     * Gets parameter value from HttpServletRequest.
     * 
     * @param req
     * @param name
     * @return
     */
    protected Object getSessionAttribute(Object req, String name) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getSession().getAttribute(name);
    }
}
