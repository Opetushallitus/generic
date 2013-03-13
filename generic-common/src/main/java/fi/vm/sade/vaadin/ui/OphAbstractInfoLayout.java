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
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.constants.StyleEnum;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.dto.PageNavigationDTO;
import fi.vm.sade.vaadin.constants.UiMarginEnum;
import fi.vm.sade.vaadin.util.UiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jani
 */
public abstract class OphAbstractInfoLayout<T extends AbstractLayout> extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(OphAbstractInfoLayout.class);

    private HorizontalLayout buttonsHL = UiUtil.horizontalLayout(true, UiMarginEnum.NONE, UiConstant.DEFAULT_RELATIVE_SIZE, "40px");

    private Class<T> _layoutClass;
    private T layout;

    private String _pageTitle;
    private String _message;
    private PageNavigationDTO _pageNavigationDTO;
    private boolean _attached;

    public OphAbstractInfoLayout(Class<T> layoutClass, String pageTitle, String message, PageNavigationDTO dto) {
        if (layoutClass == null) {
            throw new RuntimeException("An invalid constructor argument - layout class argument cannot be null.");
        }
        _layoutClass = layoutClass;
        _message = message;
        _pageTitle = pageTitle;
        _pageNavigationDTO = dto;
    }

    /**
     * Implement this is subclass to create the actual "aplication" layout.
     *
     * @param layout
     */
    protected abstract void buildLayout(T layout);


    @Override
    final public void attach() {
        super.attach();

        if (_attached) {
            return;
        }
        _attached = true;

        LOG.info("attach()");

        setMargin(true);
        VerticalLayout topArea = UiUtil.verticalLayout(UiConstant.PCT100, "120px");

        if (_message != null) {
            UiUtil.label(topArea, _message);
        }

        topArea.addComponent(buttonsHL);
        Label title = UiUtil.label(topArea, _pageTitle);
        title.setStyleName(Oph.LABEL_H1);
        HorizontalLayout buildNavigation = buildNavigation(_pageNavigationDTO);
        topArea.addComponent(buildNavigation);

        addComponent(topArea);

        try {
            layout = _layoutClass.newInstance();
            layout.setSizeFull();

            // Call subclass to do it's work
            buildLayout(layout);

            addComponent(layout);
            addComponent(buildNavigation(_pageNavigationDTO));

            topArea.setComponentAlignment(buildNavigation, Alignment.TOP_LEFT);
            setComponentAlignment(topArea, Alignment.TOP_LEFT);
            setComponentAlignment(layout, Alignment.TOP_LEFT);

            setExpandRatio(layout, 1f);
        } catch (Exception ex) {
            LOG.error("Application error - abstract class cannot initialize given class.", ex);
        }
    }

    private HorizontalLayout buildNavigation(PageNavigationDTO dto) {
        HorizontalLayout hl = UiUtil.horizontalLayout();
        if (dto == null) {
            LOG.debug("No navigation links added to layout.");
            return hl;
        }

        if (dto.getBtnNext() == null || dto.getBtnPrevious() == null) {
            throw new RuntimeException("Invalid input data, cannot create page layout.");
        }

        Button next = initButton(dto.getBtnNext().getCaption(), dto.getBtnNext().getListener(), hl);

        Label newLabel = UiUtil.label(hl, dto.getMiddleResultText());
        newLabel.setStyleName(Oph.LABEL_H2);

        Button prev = initButton(dto.getBtnPrevious().getCaption(), dto.getBtnPrevious().getListener(), hl);

        hl.setExpandRatio(newLabel, 1f);

        hl.setComponentAlignment(newLabel, Alignment.TOP_CENTER);
        hl.setComponentAlignment(prev, Alignment.TOP_RIGHT);
        hl.setComponentAlignment(next, Alignment.TOP_LEFT);

        return hl;
    }

    private Button initButton(String caption, ClickListener listenerNext, AbstractLayout l) {
        return UiUtil.buttonLink(l, caption, listenerNext);
    }


    /**
     * Add navigation action button to button layout.
     *
     * @param name
     * @param listener
     * @return
     */
    public Button addNavigationButton(String name, Button.ClickListener listener) {
        return UiUtil.button(buttonsHL, name, listener);
    }

    /**
     * Add navigation action button to button layout.
     *
     * @param name
     * @param listener
     * @param styles
     * @return
     */
    public Button addNavigationButton(String name, Button.ClickListener listener, StyleEnum styles) {
        Button btn = UiUtil.button(buttonsHL, name, listener);

        if (styles != null) {
            for (String s : styles.getStyles()) {
                btn.addStyleName(s);
            }
        }

        return btn;
    }

    /**
     * Adds a horizontal separator line to the display.
     */
    public void addLayoutSplit() {
        VerticalSplitPanel split = new VerticalSplitPanel();
        split.setImmediate(false);
        split.setWidth("100%");
        split.setHeight("2px");
        split.setLocked(true);

        layout.addComponent(split);
    }
}
