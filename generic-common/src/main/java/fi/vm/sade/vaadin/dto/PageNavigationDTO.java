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

/**
 * Used to define button navigation in "view" views (result set navigation next/prev)
 *
 * @author jani
 */
public class PageNavigationDTO {

    private ButtonDTO btnPrevious;
    private String middleResultText;
    private ButtonDTO btnNext;

    public PageNavigationDTO(ButtonDTO btnPrevious, ButtonDTO btnNext, String middleResultText) {
        this.btnPrevious = btnPrevious;
        this.btnNext = btnNext;
        this.middleResultText = middleResultText;
    }

    /**
     * @return the btnNext
     */
    public ButtonDTO getBtnNext() {
        return btnNext;
    }

    /**
     * @return the btnPrevious
     */
    public ButtonDTO getBtnPrevious() {
        return btnPrevious;
    }

    /**
     * @return the middleResultText
     */
    public String getMiddleResultText() {
        return middleResultText;
    }
}