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
package fi.vm.sade.generic.common;

/**
 * Simple helper to append prefixes to the translated keys.
 *
 * @author Jukka Raanamo
 * @author mlyly
 */
public class I18NHelper {

    private StringBuilder keyBuilder = new StringBuilder();

    private int prefixLength;

    /**
     * Build translator helper with given string prefix.
     *
     * @param prefix
     */
    public I18NHelper(String prefix) {
        keyBuilder.append(prefix);
        prefixLength = keyBuilder.length();
    }

    /**
     * Create translator helper, construct prefix from classes simple name.
     *
     * @param from
     */
    public I18NHelper(Class from) {
        this(from.getSimpleName() + ".");
    }

    /**
     * Create translator helper, construct prefix from parameters classes simple name.
     *
     * @param from
     */
    public I18NHelper(Object from) {
        this(from.getClass());
    }

    /**
     * Build translation key.
     *
     * If key is like "_XXX" then the real key will be "XXX" without any prefix.
     *
     * @param key
     * @return prefix + key OR key - "underscore"
     */
    private String makeKey(String key) {
        // Check for global key
        if (key.startsWith("_")) {
            return key.substring(1);
        }

        keyBuilder.setLength(prefixLength);
        keyBuilder.append(key);
        return keyBuilder.toString();
    }

    /**
     * Get message from resource bundle.
     *
     * @param key
     * @return
     */
    public String getMessage(String key) {
        return I18N.getMessage(makeKey(key));
    }

    /**
     * Get message with parameters.
     *
     * @param key
     * @param args
     * @return
     */
    public String getMessage(String key, Object... args) {
        return I18N.getMessage(makeKey(key), args);
    }
}
