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

import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.constants.UiMarginEnum;

/**
 *
 * @author jani
 */
public class UiLayoutUtil {

    public static Panel textPanel(final String text, final String width, final String height, final AbstractLayout layout) {
        Panel panel = panel(width, height, null, layout);
        panel.addComponent(new Label(text));

        return panel;
    }

    public static Panel panel() {
        return panel(UiConstant.DEFAULT_RELATIVE_SIZE, UiConstant.DEFAULT_RELATIVE_SIZE, null, null);
    }

    public static Panel panel(final String width, final String height, final AbstractLayout panelContent) {
        return panel(width, height, panelContent, null);
    }

    public static Panel panel(final String width, final String height, final AbstractLayout panelContent, final AbstractLayout addtoLayout) {
        Panel panel = new Panel();
        UiBaseUtil.handleStyle(panel, Oph.CONTAINER_SECONDARY);
        UiBaseUtil.handleWidth(panel, width);
        UiBaseUtil.handleHeight(panel, height);

        if (panelContent != null) {
            //when layout param is null, the Panel uses vetical layout
            panel.setContent(panelContent);
        }

        UiBaseUtil.handleAddComponent(addtoLayout, panel);

        return panel;
    }

    private static CssLayout cssLayout(final Boolean[] margin) {
        CssLayout layout = new CssLayout();
        //SPACING NOT AVAILABLE IN CSS LAYOUT CLASS
        UiBaseUtil.handleWidth(layout, null);
        UiBaseUtil.handleHeight(layout, null);
        UiBaseUtil.handleMarginParam(layout, margin); //set padding size (18px in Raideer theme) , if needed.

        return layout;
    }

    public static CssLayout cssLayout(final UiMarginEnum margin) {
        return cssLayout(margin != null ? margin.getSelectedValue() : null);
    }

    /*
     *
     * VERTICAL LAYOUT HELPER METHODS
     *
     */
    public static VerticalLayout verticalLayout() {
        return verticalLayout(null, null);
    }

    public static VerticalLayout verticalLayout(final String width, final String height) {
        return verticalLayout(false, new Boolean[]{false}, width, height);
    }

    public static VerticalLayout verticalLayout(boolean spacing, final UiMarginEnum margin) {
        return verticalLayout(spacing, margin != null ? margin.getSelectedValue() : null, null, null);
    }

    private static VerticalLayout verticalLayout(boolean spacing, final Boolean[] margin, final String width, final String height) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(spacing); //set component spacing (12px in Raideer theme)
        UiBaseUtil.handleWidth(layout, width);
        UiBaseUtil.handleHeight(layout, height);
        UiBaseUtil.handleMarginParam(layout, margin); //set padding size (18px in Raideer theme) , if needed.

        return layout;
    }

    /*
     *
     * HORIZONTAL LAYOUT HELPER METHODS
     *
     */
    /**
     * Create new instance of HorizontalLayout. No spacing and not margin.
     *
     * @return HorizontalLayout instance
     */
    public static HorizontalLayout horizontalLayout() {
        return horizontalLayout(false, new Boolean[]{false});
    }

    /**
     * Create new instance of HorizontalLayout. Optional spacing and margin
     * parameters.
     *
     * @param boolean spacing
     * @param UiMarginEnum
     * @return HorizontalLayout instance
     */
    public static HorizontalLayout horizontalLayout(boolean spacing, final UiMarginEnum margin) {
        return horizontalLayout(spacing, margin != null ? margin.getSelectedValue() : null);
    }

    /**
     * Create new instance of HorizontalLayout. Optional spacing, margin, width
     * and height parameters.
     *
     * @param spacing
     * @param margin
     * @param String width
     * @param String height
     * @return HorizontalLayout instance
     */
    public static HorizontalLayout horizontalLayout(boolean spacing, final UiMarginEnum margin, final String width, final String height) {
        return horizontalLayout(spacing, margin != null ? margin.getSelectedValue() : null, width, height);
    }

    /**
     *
     *
     * @param String width, when set to null it uses a default value.
     * @param String height, when set to null it uses a default value.
     * @param Boolean Array, you can enable the margins only for specific sides
     * by array index: top[0], right[1], bottom[2], and left[3] margin.
     * @return New instance of HorizontalLayout.
     */
    private static HorizontalLayout horizontalLayout(boolean spacing, final Boolean[] margin, final String width, final String height) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setImmediate(false);
        layout.setSpacing(spacing); //set component spacing (12px in Raideer theme)
        UiBaseUtil.handleWidth(layout, width);
        UiBaseUtil.handleHeight(layout, height);
        UiBaseUtil.handleMarginParam(layout, margin); //set padding size (18px in Raideer theme) , if needed.

        return layout;
    }

    private static HorizontalLayout horizontalLayout(boolean spacing, final Boolean[] margin) {
        //height is set to use relative size.
        return horizontalLayout(spacing, margin, null, UiConstant.DEFAULT_RELATIVE_SIZE);
    }

    /**
     * Create new tabsheet.
     *
     * @param layout
     * @return
     */
    public static TabSheet tabSheet(AbstractOrderedLayout layout) {
        TabSheet tabs = new TabSheet();
        UiBaseUtil.handleAddComponent(layout, tabs);
        return tabs;
    }

    /**
     * Add vertical locked splitter.
     *
     * @param layout
     * @return VerticalSplitPanel
     */
    public static VerticalSplitPanel horizontalLine(AbstractComponentContainer layout) {
        VerticalSplitPanel split = new VerticalSplitPanel();
        split.setImmediate(false);
        split.setWidth("100%");
        split.setHeight("2px");
        split.setLocked(true);

        UiBaseUtil.handleAddComponent(layout, split);

        return split;
    }

    /**
     * Add vertical locked splitter.
     *
     * @param layout
     * @return VerticalSplitPanel
     */
    public static VerticalSplitPanel hr(AbstractComponentContainer layout) {
        return horizontalLine(layout);
    }
}
