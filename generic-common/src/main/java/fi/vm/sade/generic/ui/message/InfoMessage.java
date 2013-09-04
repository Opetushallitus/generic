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
package fi.vm.sade.generic.ui.message;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author tommiha
 * 
 */
public class InfoMessage extends VerticalLayout {

    private static final long serialVersionUID = -6495162299981290991L;

    private boolean hasMessages = false;

    public InfoMessage() {
        setWidth("100%");
        addStyleName("info-container");
        setVisible(false);
    }

    public void addMessage(String message) {
        Label label = new Label(message);
        label.addStyleName("info-message");
        label.setWidth("100%");
        addComponent(label);
        hasMessages = true;
        setVisible(true);
    }

    public void resetMessages() {
        hasMessages = false;
        setVisible(false);
        removeAllComponents();
    }

    public boolean hasMessages() {
        return hasMessages;
    }
}
