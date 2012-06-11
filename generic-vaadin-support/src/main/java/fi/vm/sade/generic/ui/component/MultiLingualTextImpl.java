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

import fi.vm.sade.generic.common.validation.MultiLingualText;
import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * @author Antti Salonen
 */
public class MultiLingualTextImpl implements MultiLingualText, Serializable {

    private static final long serialVersionUID = 4674782658365L;

    private Object bean;
    private String textFiProperty;
    private String textSvProperty;
    private String textEnProperty;

    public MultiLingualTextImpl() {
    }

    public MultiLingualTextImpl(Object bean, String textBaseProperty) {
        this(bean, textBaseProperty + "Fi", textBaseProperty + "Sv", textBaseProperty + "En");
    }

    public MultiLingualTextImpl(Object bean, String textFiProperty, String textSvProperty, String textEnProperty) {
        this.bean = bean;
        this.textFiProperty = textFiProperty;
        this.textSvProperty = textSvProperty;
        this.textEnProperty = textEnProperty;
    }

    @Override
    public String getTextFi() {
        return getProperty(bean, textFiProperty);
    }

    @Override
    public void setTextFi(String textFi) {
        setProperty(bean, textFiProperty, textFi);
    }

    @Override
    public String getTextSv() {
        return getProperty(bean, textSvProperty);
    }

    @Override
    public void setTextSv(String textSv) {
        setProperty(bean, textSvProperty, textSv);
    }

    @Override
    public String getTextEn() {
        return getProperty(bean, textEnProperty);
    }

    @Override
    public void setTextEn(String textEn) {
        setProperty(bean, textEnProperty, textEn);
    }

    @Override
    public String getClosest(Locale locale) {
        String lang = locale.getLanguage().toLowerCase();
        if (lang.equals("fi")) {
            return oneOf(getTextFi(), getTextEn(), getTextSv());
        } else if (lang.equals("sv")) {
            return oneOf(getTextSv(), getTextEn(), getTextFi());
        } else {
            return oneOf(getTextEn(), getTextFi(), getTextSv());
        }
    }

    private String oneOf(String... texts) {
        for (String text : texts) {
            if (text != null && text.trim().length() > 0) {
                return text;
            }
        }
        return null;
    }

    private String getProperty(Object bean, String property) {
        try {
            return BeanUtils.getProperty(bean, property);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setProperty(Object bean, String property, String value) {
        try {
            BeanUtils.setProperty(bean, property, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean allAreNull() {
        return getTextFi() == null && getTextSv() == null && getTextEn() == null;
    }

    @Override
    public String toString() {
        if (bean != null) {
            return "MLText[fi="+getTextFi()+", sv="+getTextSv()+", en="+getTextEn()+"]";
        } else {
            return "MLText[bean=null]";
        }
    }
}
