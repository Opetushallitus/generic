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

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import fi.vm.sade.vaadin.constants.StyleEnum;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.constants.UiMarginEnum;
import fi.vm.sade.vaadin.util.UiUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a "navigation" layout.
 *
 * "buildLayout" is called on component attach.
 *
 * Layout of this component is
 * <pre>
 * VERTICAL LAYOUT (== this)
 * - VL
 * -- HL BUTTONS TOP
 * - LAYOUT CREATED BY GIVEN CLASS (contect created by implementing subclasses "buildLayout" method)
 * - VL
 * -- HL BUTTONS BOTTOM
 * </pre>
 *
 * @author jani
 */
public abstract class OphAbstractNavigationLayout<ABSTRACT_LAYOUT extends AbstractLayout> extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(OphAbstractNavigationLayout.class);
    private HorizontalLayout hlTopButtons = UiUtil.horizontalLayout(true, UiMarginEnum.NONE, UiConstant.DEFAULT_RELATIVE_SIZE, "40px");
    private ABSTRACT_LAYOUT layout;
    private HorizontalLayout hlBottomButtons = UiUtil.horizontalLayout(true, UiMarginEnum.NONE, UiConstant.DEFAULT_RELATIVE_SIZE, "40px");
    private Class<ABSTRACT_LAYOUT> layoutClass;
    private Map< Button.ClickListener, Set<Button>> buttons = new HashMap< Button.ClickListener, Set<Button>>();
    boolean _attached = false;

    public OphAbstractNavigationLayout(Class<ABSTRACT_LAYOUT> layoutClass) {
        if (layoutClass == null) {
            throw new RuntimeException("An invalid constructor argument - layout class argument cannot be null.");
        }
        setMargin(true);

        this.layoutClass = layoutClass;
    }

    /**
     * A layout area for UI components. Subclasses should implement their own UI
     * building to this layout. This method is called in "attach".
     *
     * Note that when you call "addComponent" it will add the component to the
     * created inner layout.
     *
     * @param layout
     */
    protected abstract void buildLayout(ABSTRACT_LAYOUT layout);

    @Override
    public void attach() {
        super.attach();

        if (_attached) {
            return;
        }
        _attached = true;

        VerticalLayout vlTop = buildButtonLayout(hlTopButtons, Alignment.TOP_LEFT);
        VerticalLayout vlBottom = buildButtonLayout(hlBottomButtons, Alignment.BOTTOM_LEFT);

        try {
            super.addComponent(vlTop);

            layout = layoutClass.newInstance();
            layout.setSizeFull();
            super.addComponent(layout);

            super.addComponent(vlBottom);

            // Call subclass to create UI
            buildLayout(layout);
            setComponentAlignment(layout, Alignment.TOP_LEFT);
            setExpandRatio(layout, 1f);
        } catch (Exception ex) {
            throw new RuntimeException("Application error - abstract class cannot initialize layout class.", ex);
        }
    }

    /**
     * Get a component container for "application" between the navigation buton
     * bars.
     *
     * @return AbstractComponentContainer
     */
    protected ABSTRACT_LAYOUT getLayout() {
        return layout;
    }

    /**
     * Add component to inner "application" layout.
     */
    @Override
    public void addComponent(Component c) {
        if (layout == null) {
            throw new IllegalStateException("No layout created yet... are you in constructor? DO the ui building in buildLayout method...");
        }
        layout.addComponent(c);
    }

    /**
     * Add control buttons to top and bottom navigation bars.
     *
     * @param name
     * @param listener
     */
    public void addNavigationButton(String name, Button.ClickListener listener) {
        addNavigationButton(name, listener, null);
    }

    /**
     * Add control buttons to top and bottom navigation bars.
     *
     * @param caption
     * @param listener
     * @param styles
     */
    public void addNavigationButton(String caption, Button.ClickListener listener, StyleEnum styles) {
        addNavigationButton(caption, listener, styles, true);
    }

    /**
     * Add navigation button to top and botton navaigation bars.
     *
     * @param caption
     * @param listener
     * @param styles
     * @param enabled
     */
    public void addNavigationButton(String caption, Button.ClickListener listener, StyleEnum styles, boolean enabled) {
        Button b;

        b = addButton(hlTopButtons, caption, listener, styles);
        b.setEnabled(enabled);

        b = addButton(hlBottomButtons, caption, listener, styles);
        b.setEnabled(enabled);
    }


    private Button addButton(AbstractLayout innerLayout, String name, Button.ClickListener listener, StyleEnum styles) {
        Button btn = UiUtil.button(innerLayout, name, listener);

        if (buttons.containsKey(listener)) {
            buttons.get(listener).add(btn);
        } else {
            //empty set
            Set<Button> set = new HashSet<Button>();
            set.add(btn);
            buttons.put(listener, set);
        }

        if (styles != null) {
            for (String s : styles.getStyles()) {
                btn.addStyleName(s);
            }
        }

        return btn;
    }

    /**
     * Build button layout.
     *
     * @param btnLayout
     * @param alignmnet
     */
    private VerticalLayout buildButtonLayout(HorizontalLayout btnLayout, Alignment alignmnet) {
        VerticalLayout vl = UiUtil.verticalLayout(UiConstant.PCT100, UiConstant.DEFAULT_RELATIVE_SIZE);
        vl.addComponent(btnLayout);
        vl.setComponentAlignment(btnLayout, alignmnet);

        return vl;

    }

    /**
     * Get a button instance by given listener.
     */
    public Set<Button> getButtonByListener(Button.ClickListener listener) {
        if (listener == null) {
            throw new RuntimeException("Application error -  listener object cannot be null.");
        }

        if (buttons.containsKey(listener)) {
            return buttons.get(listener);
        }
        return null;
    }
    
    /**
     * Enable or disable a button by given listener.
     * 
     * @param listener
     * @param enabled 
     */
    public void enableButtonByListener(Button.ClickListener listener, boolean enabled) {
        if (listener == null) {
            throw new RuntimeException("Application error -  listener object cannot be null.");
        }

        if (buttons.containsKey(listener)) {
           for(Button btn : buttons.get(listener)){
               btn.setEnabled(enabled);
           }
        }
    }
    
    
    /**
     * Change a button visibility by given listener.
     * 
     * @param listener
     * @param visible 
     */
    public void visibleButtonByListener(Button.ClickListener listener, boolean visible) {
        if (listener == null) {
            throw new RuntimeException("Application error -  listener object cannot be null.");
        }

        if (buttons.containsKey(listener)) {
           for(Button btn : buttons.get(listener)){
               btn.setVisible(visible);
           }
        }
    }

    /**
     * Get all button instances.
     */
    public Map<Button.ClickListener, Set<Button>> getAllButtons() {
        return buttons;
    }
}
