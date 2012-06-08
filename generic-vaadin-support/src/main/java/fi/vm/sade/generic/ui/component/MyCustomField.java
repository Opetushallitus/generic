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

import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Antti
 */
public class MyCustomField extends CustomField {

    private static final long serialVersionUID = 2734827492749294L;

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected Map<String, Field> fields = new HashMap<String, Field>();
    protected CustomLayout layout;
    private Class type;

    public MyCustomField(Class type, CustomLayout layout) {
        super();
        this.type = type;
        this.layout = layout;
        setCompositionRoot(layout);
    }

    protected void initFields() {
        for (String fieldKey : fields.keySet()) {
            Field field = fields.get(fieldKey);
            layout.addComponent(field, fieldKey);
        }
    }

    protected <T extends Field> T addChildField(String id, T field) {
        fields.put(id, field);
        return field;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Object getValue() {
        Property dataSource = getPropertyDataSource();
        return dataSource == null ? null : dataSource.getValue();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        Object value = newDataSource.getValue();
        for (String fieldKey : fields.keySet()) {
            Field field = fields.get(fieldKey);
            field.setPropertyDataSource(new NestedMethodProperty(value, fieldKey));
        }
    }

    public CustomLayout getLayout() {
        return layout;
    }

    @Override
    public void setImmediate(boolean immediate) {
        super.setImmediate(immediate);
        for (String fieldKey : fields.keySet()) {
            Field field = fields.get(fieldKey);
            if (field instanceof AbstractComponent) {
                ((AbstractComponent) field).setImmediate(immediate);
            }
        }
    }

    @Override
    public void setDebugId(String id) {
        super.setDebugId(id);
        for (String fieldKey : fields.keySet()) {
            Field field = fields.get(fieldKey);
            field.setDebugId(id + "_" + fieldKey);
        }
    }

    public List getFields() {
        return new ArrayList(fields.values());
    }
}
