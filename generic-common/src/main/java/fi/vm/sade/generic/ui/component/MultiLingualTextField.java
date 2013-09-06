/*
 *
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

package fi.vm.sade.generic.ui.component;

import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.TextField;

import fi.vm.sade.generic.common.validation.MultiLingualText;
import fi.vm.sade.generic.ui.CustomVaadinUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Composite component for multilingual texts, contains 3 textfields (fi,en,sv).
 *
 * NOTE:
 * - PropertyDataSource must point to property of type MultiLingualText
 *
 * @author Antti Salonen
 */
public class MultiLingualTextField extends MyCustomField {

    private static final long serialVersionUID = 7875812764891764L;
    
    private static final Logger LOG = LoggerFactory.getLogger(MultiLingualTextField.class);

    public static String LAYOUTS_MULTILINGUALTEXTFIELD = "layouts/MultiLingualTextField.html";
    public static String LAYOUTS_MULTILINGUALTEXTFIELD_SV = "layouts/MultiLingualTextField_sv.html";
    
    private TextField textFi = addChildField(MultiLingualText.PROPERTY_TEXT_FI, new TextField());
    private TextField textSv = addChildField(MultiLingualText.PROPERTY_TEXT_SV, new TextField());
    private TextField textEn = addChildField(MultiLingualText.PROPERTY_TEXT_EN, new TextField());

    private MultiLingualText mlText;
    
    public MultiLingualTextField() {
        super(null, CustomVaadinUtils.getCustomLayout(LAYOUTS_MULTILINGUALTEXTFIELD));
        
        initField();
    }


    public MultiLingualTextField(Locale locale) {
        super(null);

        if (locale != null && locale.getLanguage().equals("sv")) {
        	setLayout(CustomVaadinUtils.getCustomLayout(LAYOUTS_MULTILINGUALTEXTFIELD_SV));	
        } else {
        	setLayout(CustomVaadinUtils.getCustomLayout(LAYOUTS_MULTILINGUALTEXTFIELD));
        }
        
        initField();
    }
	
    private void initField() {
		configure(textFi);
        configure(textSv);
        configure(textEn);

        initFields();
	}
    
    private void configure(TextField textField) {
        textField.setWidth("100%");
        textField.setNullRepresentation("");
        // need to follow valuechanges so immediate on-the-fly validation and errorindicator works correctly
        textField.addListener(new ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (mlText != null) {
                    fireValueChange(false);
                }
            }
        });

    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        setDataSource((MultiLingualText) newDataSource.getValue());
    }

    public void setDataSource(MultiLingualText value) {
        mlText = value;
        textFi.setPropertyDataSource(new NestedMethodProperty(mlText, MultiLingualText.PROPERTY_TEXT_FI));
        textSv.setPropertyDataSource(new NestedMethodProperty(mlText, MultiLingualText.PROPERTY_TEXT_SV));
        textEn.setPropertyDataSource(new NestedMethodProperty(mlText, MultiLingualText.PROPERTY_TEXT_EN));
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        mlText.setTextFi(value(textFi));
        mlText.setTextSv(value(textSv));
        mlText.setTextEn(value(textEn));
        super.commit();
    }

    private String value(TextField textField) {
        return textField.getValue() != null ? textField.getValue().toString() : null;
    }

    public TextField getTextFi() {
        return textFi;
    }

    public TextField getTextSv() {
        return textSv;
    }

    public TextField getTextEn() {
        return textEn;
    }

    @Override
    public Object getValue() {
        return mlText;
    }
}
