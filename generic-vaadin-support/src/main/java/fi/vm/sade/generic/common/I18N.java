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
package fi.vm.sade.generic.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;

/**
 * @author tommiha
 *
 */
public class I18N implements ApplicationContextAware {

    private static MessageSourceAccessor messageSourceAccessor;
    private static final ThreadLocal<Locale> LOCALE_THREAD_LOCAL = new ThreadLocal<Locale>();
    private static final Locale DEFAULT_LOCALE = new Locale("fi");

    public static void setMessageSourceAccessor(MessageSourceAccessor msa) {
        messageSourceAccessor = msa;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        setMessageSourceAccessor(new MessageSourceAccessor(applicationContext));
    }

    /**
     * Retrieve the message for the given code and the default Locale.
     *
     * @param code
     *            code of the message
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException
     *             if not found
     */
    public static String getMessage(String code) {
        Locale locale = LOCALE_THREAD_LOCAL.get();
        return messageSourceAccessor.getMessage(code, locale);
    }

    /**
     * Retrieve the message for the given code and the default Locale.
     *
     * @param code
     *            code of the message
     * @param args
     *            arguments for the message, or <code>null</code> if none
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException
     *             if not found
     */
    public static String getMessage(String code, Object... args) {
        Locale locale = LOCALE_THREAD_LOCAL.get();
        return messageSourceAccessor.getMessage(code, args, locale);
    }

    public static void setLocale(Locale locale) {
        LOCALE_THREAD_LOCAL.set(locale);
    }

    public static Locale getLocale() {
        Locale locale = LOCALE_THREAD_LOCAL.get();
        if (locale == null) {
            return DEFAULT_LOCALE;
        }
        return locale;
    }
}
