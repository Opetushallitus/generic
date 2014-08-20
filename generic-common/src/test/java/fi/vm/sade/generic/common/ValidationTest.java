/*
 *
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
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

import fi.vm.sade.generic.common.validation.ValidationConstants;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Test validation
 *
 * @author markus
 */
public class ValidationTest {

    @Test
    public void testValidEmail() {
        String[] emails = {
            "niceandsimple@example.com",
            "very.common@example.com",
            "a.little.lengthy.but.fine@dept.example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-dash@example.com",
            "a_b@example.com",
            "12345@123.example.com",
            "nimi@domain.fi",
            "name@domain.museum",
            "email+symbol!.#$%&'*/=?^`{|}~@example.com"
        };
        for (String email : emails) {
            Assert.assertTrue(String.format("Email address [%s] should be valid", email), email.matches(ValidationConstants.EMAIL_PATTERN));
        }
    }

    @Test
    public void testInvalidEmail() {
        String[] emails = {
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email.@example.com",
            "email..email@example.com",
            "あいうえお@example.com",
            "email@example.com (Joe Smith)",
            "email@example",
            "email@-example.com",
            "email@111.222.333.44444",
            "email@example..com",
            "Abc..123@example.com",
            "\"(),:;<>[\\]@example.com"
        };
        for (String email : emails) {
            Assert.assertTrue(String.format("Email address [%s] should be invalid", email), !email.matches(ValidationConstants.EMAIL_PATTERN));
        }
    }

}
