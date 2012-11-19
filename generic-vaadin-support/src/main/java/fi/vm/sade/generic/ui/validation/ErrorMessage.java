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

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * @author tommiha
 * 
 */
public class ErrorMessage extends CssLayout {

    private static final long serialVersionUID = -6495162299981290991L;

    private boolean hasErrors = false;

    public ErrorMessage() {
        addStyleName("error-container");
        setVisible(false);
    }

    public ErrorMessage(InvalidValueException e) {
        addError(e);
    }

    public void addError(InvalidValueException e) {
        if (e.getCauses() == null) {
            addError(e.getMessage());
        } else {
            for (InvalidValueException causes : e.getCauses()) {
                addError(causes.getMessage());
            }
        }
    }

    public void addError(String error) {
        Label errorLabel = new Label(error);
        errorLabel.addStyleName("error");
        errorLabel.setWidth("100%");
        addComponent(errorLabel);
        hasErrors = true;
        setVisible(true);
    }

    public void resetErrors() {
        hasErrors = false;
        setVisible(false);
        removeAllComponents();
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
