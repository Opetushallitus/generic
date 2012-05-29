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

package fi.vm.sade.generic.ui;

import com.vaadin.ui.AbstractComponent;
import fi.vm.sade.generic.common.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.formbinder.PropertyId;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Antti Salonen
 */
public final class ValidationUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationUtils.class);

    private ValidationUtils() {
    }

    /**
     * Adds validators to form's vaadin Component fields, and sets them required.
     * Uses PropertyId annotations to decide how fields should be validated.
     * PropertyId must point to DTO class field, which must be annotated with JSR-303 validation constraints.
     *
     * @param form object containing vaadin fields as java fields
     * @param modelClass model's (where PropertyId -annotations point) class
     */
    public static void addValidatorsAndSetRequired(Object form, Class modelClass) {
        List<Field> fields = ClassUtils.getDeclaredFields(form.getClass());
        for (Field field : fields) {
            PropertyId propertyId = field.getAnnotation(PropertyId.class);
            if (propertyId != null) {
                try {
                    // get vaadin component
                    field.setAccessible(true);
                    com.vaadin.ui.Field component = (com.vaadin.ui.Field) field.get(form);

                    // get DTO where PropertyId -annoations points to
                    String prop = propertyId.value();
                    int a = prop.indexOf(".");
                    String dtoPropertyInModel = prop.substring(0, a);
                    String fieldPropertyInDto = prop.substring(a+1);
                    Field dtoField = ClassUtils.getDeclaredField(modelClass, dtoPropertyInModel);
                    Class dtoClass = dtoField.getType();

                    // add validator and set required
                    addValidatorAndSetRequired(component, dtoClass, fieldPropertyInDto);
                } catch (Exception e) {
                    // should we fail here or not?
                    String msg = "failed to add validator to form's component field, form: " + form + ", modelClass: " + modelClass
                            + ", field: " + field + ", proertyId: " + propertyId + ", exception: " + e;
                    throw new RuntimeException(msg, e);
//                    LOG.warn(msg, e);
                }
            }
        }
    }

    /**
     * Adds validators to single Vaadin componentField based on javafield's JSR-303 annotations,
     * and sets it required if needed (if javafield has @NotNull-annotation).
     *
     * @param componentField vaadin field component to add the validator
     * @param dtoClass class that contains the javafield
     * @param fieldName javafield that is target of the validation, and is annotated with JSR-303 validation annotations
     */
    public static void addValidatorAndSetRequired(com.vaadin.ui.Field componentField, Class dtoClass, String fieldName) {
        Field field = ClassUtils.getDeclaredField(dtoClass, fieldName);
        setRequiredIfNeeded(componentField, field);
        addValidator(componentField, field);
    }

    /**
     * Adds validators to single Vaadin componentField based on javafield's JSR-303 annotations.
     *
     * @param componentField vaadin field component to add the validator
     * @param field javafield that is target of the validation, and is annotated with JSR-303 validation annotations
     */
    public static void addValidator(com.vaadin.ui.Field componentField, Field field) {
        // validator
        componentField.addValidator(new CustomBeanValidationValidator(field.getDeclaringClass(), field.getName()));
        // set immediate
        if (componentField instanceof AbstractComponent) {
            ((AbstractComponent) componentField).setImmediate(true);
        }
        // debug
        LOG.info("added validator to vaadin componentField, componentField: "+componentField+", field: "+field);
    }

    /**
     * Sets vaadin componentfield required if needed (if javafield has @NotNull-annotation).
     *
     * @param componentField vaadin field component to add the validator
     * @param field javafield that will be used to check if component should be required
     */
    public static boolean setRequiredIfNeeded(com.vaadin.ui.Field componentField, Field field) {
        // set required if needed (if dto is @NotNull in the model, and field is @NotNull in the dto)
        boolean required = field.getAnnotation(NotNull.class) != null;
        if (required) {
            componentField.setRequired(true);
        }
        return required;
    }

}
