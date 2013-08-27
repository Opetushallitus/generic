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

import java.net.SocketException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.vm.sade.security.SadeUserDetailsWrapper;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.Terminal;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.UserError;
import com.vaadin.terminal.VariableOwner;
import com.vaadin.terminal.gwt.server.ChangeVariablesErrorEvent;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.ui.feature.UserFeature;
import fi.vm.sade.generic.ui.portlet.security.User;
import fi.vm.sade.vaadin.util.UiUtil;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Super class for sade vaadin based vaadin applications, handles locale.
 *
 * @author Jukka Raanamo
 * @author Marko Lyly
 */
public abstract class AbstractSadeApplication extends Application implements HttpServletRequestListener, ApplicationContext.TransactionListener {

    private static final long serialVersionUID = 1L;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String MDC_USER = "user";

    public static final String DEFAULT_LOCALE = "fi_FI";

    protected static ThreadLocal<User> userThreadLocal = new ThreadLocal<User>();

    /**
     * When overriding this method, remember to call super as the first thing.
     */
    @Override
    public void init() {
        setErrorHandler(this);
    }

    public User getUser() {
        return UserFeature.get();
    }

    @Override
    public void setUser(Object user) {
        throw new RuntimeException("DO NOT USE SET USER, THIS HAS BEEN OVERWRITTEN IN AbstractSadeApplication");
    }

    /*
     * override Application.setLocale to set locale also to I18N and to the
     * Spring framework locale context holder.
     */
    @Override
    public void setLocale(Locale locale) {
        // DEBUGSAWAY:log.debug("setLocale({})", locale);

        super.setLocale(locale);
    }

    private void setLang(String lang) {
        if (StringUtils.isNotBlank(lang)) {
            Locale locale = new Locale(lang);
            setLocale(locale);
        }
    }

    private void setUserLocale(Locale locale) {
        if (locale == null) {
            locale = new Locale(DEFAULT_LOCALE);
            // DEBUGSAWAY:log.debug("locale was null, defaulting({})", locale);
        }
        I18N.setLocale(locale);
        LocaleContextHolder.setLocale(locale);
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

    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        final Throwable t = event.getThrowable();
        String stamp = String.format("%s", System.currentTimeMillis());
        if (getMainWindow() != null) {
            
            final Window errorPopup = new Window(I18N.getMessage("error"));
            errorPopup.setClosable(false);
            VerticalLayout vl = new VerticalLayout();
            vl.setWidth("380px");
            vl.setSpacing(true);
            vl.setMargin(true);
            VerticalLayout vl1 =new VerticalLayout();
            vl1.setSizeFull();
            vl1.addStyleName("error-container");
            vl1.setSpacing(true);
            Label unexpectedLabel = new Label(I18N.getMessage("unexpectedError", stamp));
            unexpectedLabel.addStyleName("error");
            unexpectedLabel.setWidth("100%");
            vl1.addComponent(unexpectedLabel);
            vl1.setComponentAlignment(unexpectedLabel, Alignment.MIDDLE_CENTER);
            vl.addComponent(vl1);
            Button okButton = UiUtil.button(vl, I18N.getMessage("OK"), new Button.ClickListener() {

                private static final long serialVersionUID = 6028471405922131311L;

                @Override
                public void buttonClick(ClickEvent event) {
                    if (errorPopup != null) {
                        getMainWindow().removeWindow(errorPopup);
                    }
                }
            });
           
            
            vl.setComponentAlignment(vl1, Alignment.MIDDLE_CENTER);
            vl.setComponentAlignment(okButton, Alignment.BOTTOM_CENTER);
            
            errorPopup.setContent(vl);
            errorPopup.setModal(true);
            errorPopup.center();
            
            
            getMainWindow().addWindow(errorPopup);
            
            //getMainWindow().showNotification(I18N.getMessage("unexpectedError") + "\n" + I18N.getMessage("unexpectedErrorCode",stamp), Notification.TYPE_ERROR_MESSAGE);

            if (t instanceof SocketException) {
                // Most likely client browser closed socket
                log.info(
                        "SocketException in CommunicationManager."
                                + " Most likely client (browser) closed socket.");
                return;
            }

            // Finds the original source of the error/exception
            Object owner = null;
            if (event instanceof VariableOwner.ErrorEvent) {
                owner = ((VariableOwner.ErrorEvent) event).getVariableOwner();
            } else if (event instanceof URIHandler.ErrorEvent) {
                owner = ((URIHandler.ErrorEvent) event).getURIHandler();
            } else if (event instanceof ParameterHandler.ErrorEvent) {
                owner = ((ParameterHandler.ErrorEvent) event).getParameterHandler();
            } else if (event instanceof ChangeVariablesErrorEvent) {
                owner = ((ChangeVariablesErrorEvent) event).getComponent();
            }

            // Shows the error in AbstractComponent
            if (owner instanceof AbstractComponent) {
                ((AbstractComponent) owner).setComponentError(new UserError(I18N.getMessage("unexpectedError") + "\n" + I18N.getMessage("unexpectedErrorCode",stamp)));
            }

        }
        // also print the error on console
        log.error("Terminal error, code: " + stamp, t);
    }


    /*
     * Implement HttpServletRequestListener interface
     */
    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        log.debug("onRequestStart()");

        // NO SUPER
//        User user = new UserLiferayImpl(request);
//        UserFeature.set(user);

        //setLocale(user.getLang());
        String lang = null;
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof SadeUserDetailsWrapper) {
           lang =  ((SadeUserDetailsWrapper)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLang();
        }
        Locale userLocale = null;
        if (lang != null) {
            userLocale = LocaleUtils.toLocale(lang);

        }  else {
            userLocale = LocaleUtils.toLocale(DEFAULT_LOCALE);
        }
        setUserLocale(userLocale);
//        // TODO "user to MDC" should be moved to correct filter when somone finds the correct location, preferably same for front/backend...
//        {
//            // Add user principal (OID) to MDC for logging if available
//            if (SecurityContextHolder.getContext().getAuthentication() != null) {
//                if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserDetails) {
//                	UserDetails ud = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                    MDC.put(MDC_USER, ud.getUsername());
//                } else {
//                	MDC.put(MDC_USER, "" + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//                }
//            } else {
//                MDC.put(MDC_USER, "-");
//            }
//        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        //log.debug("onRequestEnd()");

        // NO SUPER
//        UserFeature.remove();

//        MDC.remove(MDC_USER);
    }

    /**
     * Gets parameter value from Session.
     *
     * @param req
     * @param name
     * @return
     */
    protected Object getSessionAttribute(Object req, String name) {
        HttpServletRequest request = (HttpServletRequest) req;
        return request.getSession().getAttribute(name);
    }

    // Implement ApplicationContext.TransactionListener interface

    @Override
    public void transactionStart(Application application, Object transactionData) {
        log.debug("transactionStart()");
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        log.debug("transactionEnd()");
    }
}
