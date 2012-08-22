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

import fi.vm.sade.generic.common.I18N;

/**
 * Super class for sade vaadin based vaadin applications, handles locale.
 * 
 * @author Jukka Raanamo
 * @author Marko Lyly
 */
public abstract class AbstractSadeApplication extends Application implements HttpServletRequestListener {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * System default locale - defined to be "fi".
     */
    public static final String DEFAULT_LOCALE = "fi";

    protected Locale sessionLocale = new Locale(DEFAULT_LOCALE);

    /**
     * When overriding this method, remember to call super as the first thing.
     */
    @Override
    public synchronized void init() {
        log.info("init(), current locale: {}, reset to session locale: {}", I18N.getLocale(), sessionLocale);
        setLocale(sessionLocale);

    }

    /*
     * override Application.setLocale to set locale also to I18N and to the
     * Spring framework locale context holder.
     */
    @Override
    public void setLocale(Locale locale) {
        log.debug("setLocale({})", locale);
        I18N.setLocale(locale);
        LocaleContextHolder.setLocale(locale);
        super.setLocale(locale);
    }

    private void setLang(String lang) {
        if (StringUtils.isNotBlank(lang)) {
            sessionLocale = new Locale(lang);
            setLocale(sessionLocale);
        }
    }

    /*
     * Implement HttpServletRequestListener interface
     */
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        setUser(new UserLiferayImpl(request));
        setLang(getParameter(request, "lang"));
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
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
