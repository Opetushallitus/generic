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
package fi.vm.sade.generic.ui.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Validator;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public abstract class AbstractValidatingComponent extends CustomComponent implements ValidatingComponent {

    private Collection<Validator> validators = new ArrayList<Validator>();

    public void addValidator(Validator validator) {
        this.validators.add(validator);
    }

    @Override
    public List<Validator.InvalidValueException> getValidationErrors() {
        List<Validator.InvalidValueException> exceptions = new ArrayList<Validator.InvalidValueException>();
        for (Validator validator : validators) {
            try {
                validator.validate(this);
            } catch (Validator.InvalidValueException e) {
                exceptions.add(e);
            }
        }

        return exceptions;
    }
}
