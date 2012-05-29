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

package fi.vm.sade.generic.ui;

import com.vaadin.addon.beanvalidation.BeanValidationValidator;
import fi.vm.sade.generic.common.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Added better error logging to BeanValidationValidator.
 *
 * @author Antti Salonen
 */
public class CustomBeanValidationValidator extends BeanValidationValidator {

    private static final Logger LOG = LoggerFactory.getLogger(CustomBeanValidationValidator.class);

    private Class clazz;
    private String fieldName;

    public CustomBeanValidationValidator(Class clazz, String fieldName) {
        super(clazz, fieldName);
        this.clazz = clazz;
        this.fieldName = fieldName;
        setLocale(I18N.getLocale());
        LOG.debug("CustomBeanValidationValidator created, locale: " + getLocale());
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        try {
            super.validate(value);
        } catch (InvalidValueException e) {
            LOG.warn("failed to validate, clazz: "+clazz.getSimpleName()+", fieldName: "+fieldName+", value: "+value
                    +", locale: "+ Locale.getDefault()+", exception: "+e);
            throw e;
        }
    }

}
