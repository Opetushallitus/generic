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
package fi.vm.sade.vaadin.dto;

import com.vaadin.ui.Button.ClickListener;


/**
 *
 * @author jani
 */
public class ButtonDTO {

    private String caption;
    private ClickListener listener;

    public ButtonDTO(String caption, ClickListener listenerNext) {
        this.caption = caption;
        this.listener = listenerNext;
    }
    
    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the listener
     */
    public ClickListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(ClickListener listener) {
        this.listener = listener;
    }

   
}
