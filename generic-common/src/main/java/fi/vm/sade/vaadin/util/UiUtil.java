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

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import fi.vm.sade.generic.ui.component.OphRichTextArea;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.constants.LabelStyleEnum;
import fi.vm.sade.vaadin.constants.UiConstant;

/**
 * Helpers to create styled Vaadin-components.
 *
 * @author jani
 * @author mlyly
 */
public class UiUtil extends UiLayoutUtil {

    public static DateField dateField() {
        return UiUtil.dateField(null, null, null);
    }

    public static DateField dateField(final AbstractComponentContainer layout) {
        return UiUtil.dateField(layout, null, null);
    }

    public static DateField dateField(final AbstractComponentContainer layout, final String caption) {
        return UiUtil.dateField(layout, caption, null);
    }

    public static DateField dateField(final AbstractComponentContainer layout, final String caption, final String dateFormat) {
        DateField df = new DateField();
        if (caption != null) {
            df.setCaption(caption);
        }
        df.setDateFormat(dateFormat != null ? dateFormat : UiConstant.DEFAULT_DATE_FORMAT);
        UiBaseUtil.handleAddComponent(layout, df);
        return df;
    }

    public static DateField dateField(final AbstractComponentContainer layout, final String caption, final String dateFormat, final PropertysetItem psi, final String expression) {
        DateField c = dateField(layout, caption, dateFormat);

        if (psi != null && expression != null) {
            c.setPropertyDataSource(psi.getItemProperty(expression));
        }

        return c;
    }

    public static Link link(final AbstractComponentContainer layout, final String caption) {
        Link link = new Link();
        link.setCaption(caption);
        link.setImmediate(false);

        UiBaseUtil.handleAddComponent(layout, link);

        return link;
    }

    public static TextField textField(final AbstractComponentContainer layout) {
        return textField(layout, "", "", true);
    }

    public static TextField textFieldSmallSearch(final AbstractComponentContainer layout) {
        TextField textField = textField(layout, "", "", true);
        UiBaseUtil.handleStyle(textField, Oph.TEXTFIELD_SEARCH);
        return textField;
    }

    public static TextField textField(final AbstractComponentContainer layout, final String nullRepresentation, final String inputPrompt, boolean immediate) {
        TextField tf = new TextField();
        if (nullRepresentation != null) {
            tf.setNullRepresentation(nullRepresentation);
        }
        if (inputPrompt != null) {
            tf.setInputPrompt(inputPrompt);
        }
        UiBaseUtil.handleAddComponent(layout, tf);
        tf.setImmediate(immediate);
        return tf;
    }

    /**
     * Add property bound text field.
     *
     * @param psi
     * @param expression
     * @param caption
     * @param prompt
     * @param layout
     * @return
     */
    public static TextField textField(final AbstractComponentContainer layout, final PropertysetItem psi, final String expression, final String caption, final String prompt) {
        TextField tf = textField(layout, "", prompt, true);

        if (caption != null) {
            tf.setCaption(caption);
        }

        if (psi != null && expression != null) {
            tf.setPropertyDataSource(psi.getItemProperty(expression));
        }

        UiBaseUtil.handleAddComponent(layout, tf);

        return tf;
    }

    public static Label label(final String format, final Object... args) {
        return label((AbstractComponentContainer) null, format, args);
    }

    public static Label label(final AbstractComponentContainer layout, final String format, final Object... args) {
        Label label = new Label(UiBaseUtil.format(format, args));
        UiBaseUtil.handleAddComponent(layout, label);
        return label;
    }

    public static Label label(final AbstractLayout layout, final String format, final LabelStyleEnum style, final Object... args) {
        Label label = UiUtil.label(layout, format, args);
        switch (style) {
            case H1:
                label.addStyleName(Oph.LABEL_H1);
                break;
            case H2:
                label.addStyleName(Oph.LABEL_H2);
                break;
            case TEXT:
                label.addStyleName(Oph.LABEL_SMALL);
                break;
            case TEXT_RAW:
                label.addStyleName(Oph.LABEL_SMALL);
                label.setContentMode(Label.CONTENT_XHTML);
                break;
        }
        UiBaseUtil.handleAddComponent(layout, label);
        return label;
    }

    /**
     * Create propertu bound label. Default style is "Oph.LABEL_SMALL".
     *
     * @param psi
     * @param expression
     * @param layout
     * @return
     */
    public static Label label(final AbstractComponentContainer layout, final PropertysetItem psi, final String expression) {
        Label label = new Label();

        label.addStyleName(Oph.LABEL_SMALL);

        if (psi != null && expression != null) {
            label.setPropertyDataSource(psi.getItemProperty(expression));
        }

        UiBaseUtil.handleAddComponent(layout, label);

        return label;
    }

    public static Label label(final AbstractComponentContainer layout, final String caption) {
        Label label = new Label(caption);

        label.setImmediate(false);
        UiBaseUtil.handleWidth(label, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleHeight(label, UiConstant.DEFAULT_RELATIVE_SIZE);

        UiBaseUtil.handleAddComponent(layout, label);

        return label;
    }




    public static CheckBox checkbox(final AbstractComponentContainer layout, final String name) {
        CheckBox checkBox;
        if (name != null) {
            checkBox = new CheckBox(name);
        } else {
            checkBox = new CheckBox();
        }

        checkBox.setImmediate(false);
        UiBaseUtil.handleWidth(checkBox, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleHeight(checkBox, UiConstant.DEFAULT_RELATIVE_SIZE);

        UiBaseUtil.handleAddComponent(layout, checkBox);

        return checkBox;
    }

    /**
     * Create a checkbox with a caption and possibly bind to a property.
     *
     * @param caption
     * @param psi
     * @param expression
     * @param layout
     * @return
     */
    public static CheckBox checkBox(final AbstractComponentContainer layout, final String caption, final PropertysetItem psi, final String expression) {
        CheckBox cb = checkbox(layout, caption);

        // Bind
        if (psi != null && expression != null) {
            cb.setPropertyDataSource(psi.getItemProperty(expression));
        }

        return cb;
    }


    public static ComboBox comboBox(final AbstractComponentContainer layout, final String caption, final String[] items) {
        ComboBox comboBox = new ComboBox();

        if (caption != null) {
            comboBox.setCaption(caption);
        }

        if (items != null && items.length > 0) {
            for (String item : items) {
                comboBox.addItem(item);
            }
            comboBox.setValue(items[0]);
        }

        comboBox.setImmediate(false);
        comboBox.setNullSelectionAllowed(false);

        UiBaseUtil.handleWidth(comboBox, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleHeight(comboBox, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleAddComponent(layout, comboBox);

        return comboBox;
    }

    public static TwinColSelect twinColSelect() {
        return twinColSelect(null, null, null, null);
    }

    public static TwinColSelect twinColSelect(final AbstractComponentContainer layout, final String caption, final String[] items) {
        return twinColSelect(layout, caption, null, items);
    }

    public static TwinColSelect twinColSelect(final AbstractComponentContainer layout, final String caption, final Property.ValueChangeListener listener) {
        return twinColSelect(layout, caption, listener, null);
    }

    /**
     * Create TwinColSelect in immediate mode.
     *
     * @param layout
     * @param caption
     * @param listener
     * @param items
     * @return
     */
    public static TwinColSelect twinColSelect(final AbstractComponentContainer layout, final String caption, final Property.ValueChangeListener listener, final String[] items) {
        TwinColSelect tcs = new TwinColSelect(caption);

        if (listener != null) {
            tcs.addListener(listener);
        }

        if (items != null) {
            for (String item : items) {
                tcs.addItem(item);
            }
        }

        tcs.setImmediate(true);
        UiBaseUtil.handleWidth(tcs, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleHeight(tcs, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleAddComponent(layout, tcs);

        return tcs;
    }

    /**
     * Returns a small button instance.
     *
     * @param layout
     * @param caption
     * @return Vaadin button
     */
    public static Button button(final AbstractComponentContainer layout, final String caption) {
        return button(layout, caption, null);
    }

    /**
     * Returns a small button instance.
     *
     * @param layout
     * @param caption
     * @param listener
     * @return Vaadin button
     */
    public static Button button(final AbstractComponentContainer layout, final String caption, final ClickListener listener) {
        Button btn = new Button();

        // Avoid double clicks on buttons (ie. multiple calls to server)
        btn.setDisableOnClick(true);

        // Add listener to re-enable the button
        btn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (event != null && event.getButton() != null) {
                    event.getButton().setEnabled(true);
                }
            }
        });

        // Set caption
        if (caption != null) {
            btn.setCaption(caption);
        }

        // Add parameter listener
        if (listener != null) {
            btn.addListener(listener);
        }

        UiBaseUtil.handleWidth(btn, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleHeight(btn, UiConstant.DEFAULT_RELATIVE_SIZE);
        UiBaseUtil.handleStyle(btn, Oph.BUTTON_SMALL);
        UiBaseUtil.handleAddComponent(layout, btn);

        return btn;
    }

    /**
     * Returns a small orange button instance.
     *
     * @param layout
     * @param caption
     * @return Vaadin button
     */
    public static Button buttonSmallPrimary(final AbstractComponentContainer layout, final String caption) {
        return buttonSmallPrimary(layout, caption, null);
    }

    /**
     * Returns a small orange button instance.
     *
     * @param layout
     * @param caption
     * @param listener
     * @return Vaadin button
     */
    public static Button buttonSmallPrimary(final AbstractComponentContainer layout, final String caption, final ClickListener listener) {
        Button btn = button(layout, caption, listener);
        UiBaseUtil.handleStyle(btn, Oph.BUTTON_DEFAULT);

        return btn;
    }

    /**
     * Returns a small info icon button instance.
     *
     * @param layout
     * @return Vaadin button
     */
    public static Button buttonSmallInfo(final AbstractComponentContainer layout) {
        return buttonSmallInfo(layout, null);
    }

    /**
     * Returns a small info icon button instance.
     *
     * @param layout
     * @param listener
     * @return Vaadin button
     */
    public static Button buttonSmallInfo(final AbstractComponentContainer layout, final ClickListener listener) {
        Button btn = button(layout, null, listener);
        btn.setStyleName(Oph.BUTTON_INFO);

        return btn;
    }

    /**
     * Returns a small grey button instance.
     *
     * @param layout
     * @param caption
     * @return Vaadin button
     */
    public static Button buttonSmallSecodary(final AbstractComponentContainer layout, final String caption) {
        return buttonSmallSecodary(layout, caption, null);
    }

    /**
     * Returns a small grey button instance.
     *
     * @param layout
     * @param caption
     * @param listener
     * @return Vaadin button
     */
    public static Button buttonSmallSecodary(final AbstractComponentContainer layout, final String caption, final ClickListener listener) {
        Button btn = button(layout, caption, listener);
        UiBaseUtil.handleStyle(btn, Oph.CONTAINER_SECONDARY);

        return btn;
    }

    /**
     * Returns a small plus icon button instance.
     *
     * @param layout
     * @param caption
     * @return Vaadin button
     */
    public static Button buttonSmallPlus(final AbstractComponentContainer layout, final String caption) {
        return buttonSmallPlus(layout, caption, null);
    }

    /**
     * Returns a small plus icon button instance.
     *
     * @param layout
     * @param caption
     * @param listener
     * @return Vaadin button
     */
    public static Button buttonSmallPlus(final AbstractComponentContainer layout, final String caption, final ClickListener listener) {
        Button btn = button(layout, caption, listener);
        UiBaseUtil.handleStyle(btn, Oph.BUTTON_DEFAULT);
        UiBaseUtil.handleStyle(btn, Oph.BUTTON_PLUS);

        return btn;
    }

    /**
     * Returns a link button instance.
     *
     * @param layout
     * @param caption
     * @return Vaadin button
     */
    public static Button buttonLink(final AbstractComponentContainer layout, final String caption) {
        return buttonLink(layout, caption, null);
    }

    /**
     * Returns a link button instance.
     *
     * @param layout
     * @param caption
     * @param listener
     * @return Vaadin button
     */
    public static Button buttonLink(final AbstractComponentContainer layout, final String caption, final ClickListener listener) {
        Button btn = button(layout, caption, listener);
        btn.setStyleName(Oph.BUTTON_LINK);

        return btn;
    }

    /**
     * Create RichTextArea. Possibly bind to property. Rich text are is actually TinyMCE editor.
     *
     * @param psi
     * @param expression
     * @param layout
     * @return
     */
    public static OphRichTextArea richTextArea(final AbstractComponentContainer layout, final PropertysetItem psi, final String expression) {

        OphRichTextArea rta = new OphRichTextArea();
        rta.setNullRepresentation("");
        rta.setWidth(UiConstant.PCT100);
        rta.setImmediate(true);

        // Bind to model
        if (psi != null && expression != null) {
            rta.setPropertyDataSource(psi.getItemProperty(expression));
        }

        UiBaseUtil.handleAddComponent(layout, rta);

        return rta;
    }
    
    /**
     * Create RichTextArea. Possibly bind to property. Rich text are is actually TinyMCE editor.
     *
     * @param psi
     * @param expression
     * @param layout
     * @return
     */
    public static OphRichTextArea richTextArea(final AbstractComponentContainer layout, final PropertysetItem psi, final String expression, final int maxLength, String maxLengthErrorMessage) {

        OphRichTextArea rta = new OphRichTextArea(maxLength, maxLengthErrorMessage);
        rta.setNullRepresentation("");
        rta.setWidth(UiConstant.PCT100);
        rta.setImmediate(true);

        // Bind to model
        if (psi != null && expression != null) {
            rta.setPropertyDataSource(psi.getItemProperty(expression));
        }

        UiBaseUtil.handleAddComponent(layout, rta);

        return rta;
    }
    
    
}
