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
package fi.vm.sade.vaadin.constants;

/**
 *
 * @author jani
 */
public enum UiMarginEnum {

    BOTTOM(new Boolean[]{false, false, true, false}),
    LEFT(new Boolean[]{false, false, false, true}),
    RIGHT(new Boolean[]{false, true, false, false}),
    TOP(new Boolean[]{true, false, false, false}),
    TOP_LEFT(new Boolean[]{true, false, false, true}),
    TOP_RIGHT(new Boolean[]{true, true, false, false}),
    LEFT_RIGHT(new Boolean[]{false, true, false, true}),
    BOTTOM_LEFT(new Boolean[]{false, false, true, true}),
    RIGHT_BOTTOM_LEFT(new Boolean[]{false, true, true, true}),
    TOP_RIGHT_BOTTOM(new Boolean[]{true, true, true, false}),
    TOP_BOTTOM_LEFT(new Boolean[]{true, false, true, true}),
    TOP_RIGHT_LEFT(new Boolean[]{true, true, false, true}),
    TOP_BOTTOM(new Boolean[]{true, false, true, false}),
    ALL(new Boolean[]{true}),
    NONE(new Boolean[]{false});
    private Boolean[] boolArray;

    UiMarginEnum(Boolean[] styleName) {
        this.boolArray = styleName;
    }

    /**
     * @return the cssClass
     */
    public Boolean[] getSelectedValue() {
        return boolArray;
    }
}
