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
package fi.vm.sade.vaadin.ui;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.constants.UiMarginEnum;
import fi.vm.sade.vaadin.util.UiBaseUtil;
import fi.vm.sade.vaadin.util.UiUtil;

/**
 *
 * @author jani
 */
public abstract class OphAbstractWindow extends Window implements Window.CloseListener {

    private VerticalLayout windowLayout;

    public OphAbstractWindow(String label) {
        super(label);
        init("75%", UiConstant.DEFAULT_RELATIVE_SIZE, true, null, true);
    }
    
    public OphAbstractWindow(String label, boolean doLayout) {
        super(label);
       init("75%", UiConstant.DEFAULT_RELATIVE_SIZE, true, null, doLayout);
    }

    public OphAbstractWindow(String label, String width, String height, boolean modal) {
        super(label);
        init(width, height, modal, null, true);
    }

    public OphAbstractWindow(String label, String width, String height, boolean modal, VerticalLayout layout) {
        super(label);
        init(width, height, modal, layout, true);
    }

    protected void init(String width, String height, boolean modal, VerticalLayout layout, boolean doLayout) {
        UiBaseUtil.handleWidth(this, width);
        UiBaseUtil.handleHeight(this, height);
        center();
        setModal(true);

        if (layout == null) {
            windowLayout =  UiUtil.verticalLayout(true, UiMarginEnum.NONE);
        } else {
            windowLayout =  layout;
        }

        windowLayout.setSizeFull();
        this.setContent(windowLayout);

        if (doLayout) {
            buildOrder(windowLayout);
        }
    }

    protected void buildOrder(VerticalLayout layout) {
        buildLayout(layout);
    }

    public abstract void buildLayout(VerticalLayout layout);

     /**
     * Add components to VerticalLayout
     *
     * @param component
     */
    protected void addLayoutComponent(Component component) {
        windowLayout.addComponent(component);
    }
    
    @Override
    public void windowClose(CloseEvent e) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void fullContentHeight() {
        getContent().setHeight("100%");
    }

     /**
     * Get base layout.
     *
     * @return AbstractComponentContainer
     */
    @Override
    public VerticalLayout getLayout() {
        return windowLayout;
    }

    public void test() {
    }
}
