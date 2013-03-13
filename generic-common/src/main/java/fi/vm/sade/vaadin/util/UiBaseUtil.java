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
package fi.vm.sade.vaadin.util;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractLayout;
import fi.vm.sade.vaadin.constants.UiConstant;
import java.text.MessageFormat;

/**
 *
 * @author jani
 */
public class UiBaseUtil {

    public static AbstractLayout handleMarginParam(final AbstractLayout layout, final Boolean[] margin) {
        if (margin != null && margin.length == 1) {
            layout.setMargin(margin[0]);
        } else if (margin != null && margin.length >= 4) {
            layout.setMargin(margin[0], margin[1], margin[2], margin[3]);
        } else {
            layout.setMargin(false);
        }

        return layout;
    }

    public static AbstractComponent handleHeight(final AbstractComponent component, final String height) {
        if (height != null && height.equals(UiConstant.DEFAULT_RELATIVE_SIZE)) {
            component.setHeight(-1, Sizeable.UNITS_PIXELS);
        } else if (height != null) {
            //set CSS value
            component.setHeight(height);
        } else {
            component.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        }

        return component;
    }

    public static AbstractComponent handleWidth(final AbstractComponent component, final String width) {

        if (width != null && width.equals(UiConstant.DEFAULT_RELATIVE_SIZE)) {
            component.setWidth(-1, Sizeable.UNITS_PIXELS);
        } else if (width != null) {
            //set CSS value
            component.setWidth(width);
        } else {
            component.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        }

        return component;
    }

    public static void handleAddComponent(final AbstractComponentContainer layout, final AbstractComponent component) {
        if (layout != null) {
            layout.addComponent(component);
        }
    }

    public static void handleStyle(final AbstractComponent layout, final String styleName) {
        if (layout != null) {
            layout.addStyleName(styleName);
        }
    }

    public static final String format(final String format, final Object... args) {
        return MessageFormat.format(format, args);
    }
}
