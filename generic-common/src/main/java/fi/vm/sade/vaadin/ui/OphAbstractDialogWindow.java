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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import fi.vm.sade.vaadin.constants.LabelStyleEnum;
import fi.vm.sade.vaadin.constants.StyleEnum;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.util.UiUtil;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jani
 */
public abstract class OphAbstractDialogWindow extends OphAbstractWindow {

    private static final Logger LOG = LoggerFactory.getLogger(OphAbstractDialogWindow.class);
    private HorizontalLayout bottomLayout;
    private List<ButtonContainer> buttons;
    private Panel msgPanel;
    private Label topic;

    public OphAbstractDialogWindow(String winLabel) {
        super(winLabel, false); //false, do not build layout in parent abstract class
        setDialogMessages(null, null);
        setHeight("65%");
        setWidth("65%");
    }

    public OphAbstractDialogWindow(String winLabel, String topic, String message) {
        super(winLabel, false); //false, do not build layout in parent abstract class
        setDialogMessages(topic, message);
        setHeight("65%");
        setWidth("65%");
    }

    @Override
    public void attach() {
        super.attach();

        if (LOG.isDebugEnabled()) {
            LOG.debug("attach()");
        }
        buildLayout(getLayout());
        getLayout().setMargin(true);
    }

    public void buildDialogButtons() {
        if (buttons == null) {
            LOG.error("An initialization error, no initialised buttons.");
            return;
        }

        for (ButtonContainer c : buttons) {
            bottomLayout.addComponent(c.getBtn());
            bottomLayout.setComponentAlignment(c.getBtn(), Alignment.BOTTOM_RIGHT);
        }

        bottomLayout.setExpandRatio(buttons.get(0).getBtn(), 1f);
        getLayout().addComponent(bottomLayout);
    }

    public void addNavigationButton(final String name, final Button.ClickListener listener) {
        addNavigationButton(name, listener, null);
    }

    public void addNavigationButton(final String name, final Button.ClickListener listener, final StyleEnum styles) {
        if (bottomLayout == null) {
            //init
            bottomLayout = UiUtil.horizontalLayout();
            buttons = new ArrayList<ButtonContainer>();
        }

        Button btn = UiUtil.button(bottomLayout, name);

        if (styles != null) {
            for (String s : styles.getStyles()) {
                btn.addStyleName(s);
            }
        }

        btn.addListener(listener);
        buttons.add(new ButtonContainer(name, btn, listener));
    }

    public void removeDialogButtons() {
        for (ButtonContainer c : buttons) {
            c.getBtn().removeListener(c.getListener());
        }

        buttons.clear();
    }

    /**
     * @param topic the topic to set
     */
    protected void setDialogMessages(String topic, String message) {
        if (topic != null && this.topic == null) {
            this.topic = UiUtil.label(getLayout(), topic, LabelStyleEnum.H2);
        }

        if (message != null && this.msgPanel == null) {
            this.msgPanel = UiUtil.textPanel(message, null, UiConstant.DEFAULT_RELATIVE_SIZE, getLayout());
        }
    }

    private class ButtonContainer {

        private String name;
        private Button btn;
        private Button.ClickListener listener;

        public ButtonContainer(String name, Button btn, ClickListener listener) {
            this.name = name;
            this.btn = btn;
            this.listener = listener;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the btn
         */
        public Button getBtn() {
            return btn;
        }

        /**
         * @return the listener
         */
        public Button.ClickListener getListener() {
            return listener;
        }
    }
}
