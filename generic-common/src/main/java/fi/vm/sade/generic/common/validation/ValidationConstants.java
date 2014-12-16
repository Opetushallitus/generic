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

package fi.vm.sade.generic.common.validation;

/**
 * @author Antti Salonen
 */
public final class ValidationConstants {

    public static final int GENERIC_MIN = 3;
    public static final int GENERIC_MAX = 100;
    //public static final int YTUNNUS_LENGTH = 9;
    public static final int SHORT_MAX = 10;
    public static final int DESCRIPTION_MAX = 1000;
    public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+!#$%&'*/=?^`{|}~]+(\\.[_A-Za-z0-9-+!#$%&'*/=?^`{|}~]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    // http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java
    // See: OVT-2178, match also without http(s),ftp,file:// prefixes
    public static final String WWW_PATTERN = "^(|(https?|ftp|file)://)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public static final String ZIPCODE_PATTERN = "[0-9]{5}";
    public static final String PHONE_PATTERN = "[+|-| |\\(|\\)|[0-9]]{3,100}+";

    private ValidationConstants() {
    }

}
