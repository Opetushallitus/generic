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
package fi.vm.sade.generic.ui.validation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;

import com.vaadin.data.Validator;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;

import fi.vm.sade.generic.common.ClassUtils;
import fi.vm.sade.generic.common.I18N;

/**
 * Vaadin validator that validates based on JSR-303 annotations given to form's
 * Vaadin fields. Traditionally JSR-303 annotations are given to domain class
 * properties, but with this validator it is able to annotate the form fields.
 * <p/>
 * Usage: - Annotate form's vaadin fields with JSR-303 annotations - Call
 * JSR303FieldValidator.addValidatorsBasedOnAnnotations(form)
 * <p/>
 * HINT: - You can use same annotations in corresponding JPA model class for
 * service side validation
 * <p/>
 * When initialized in Spring context, can also find I18N keys from message sources
 * present in application context.
 * <p/>
 * Example:
 * <p/>
 * class SampleForm {
 *
 * @NotNull
 * @Size(min = 3, max = 100) private TextField nameField = ...;
 *           <p/>
 *           SampleForm() { ...
 *           JSR303FieldValidator.addValidatorsBasedOnAnnotations(this); ... } }
 *           <p/>
 *           class SampleJPA {
 * @NotNull
 * @Size(min = 3, max = 100) private String name; }
 *
 *           NOTE!
 *
 *           - If you can/want to annotate model instead of vaadin fields, use
 *           fi.vm.sade.generic.ui.ValidationUtils
 *
 * @author Antti Salonen
 */
public class JSR303FieldValidator implements Validator, ApplicationContextAware {

    /**
	 *
	 */
	private static final long serialVersionUID = -4965493581301870157L;
	private static final Logger LOG = LoggerFactory.getLogger(JSR303FieldValidator.class);
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static javax.validation.Validator javaxValidator = factory.getValidator();
    private static MessageInterpolator messageInterpolator = factory.getMessageInterpolator();

    private Set<ConstraintDescriptor<?>> constraintDescriptors;
    private ConstraintValidatorContext ctx = null; // NOTE: tätä ei oikein saa
                                                   // helposti(?) mutta
                                                   // validaattorit ei tätä
                                                   // oikeastaan näytä
                                                   // tarvitsevan

    private Object form;
    private String property;

    /**
     * Default constructor should only be called from Spring context.
     */
    public JSR303FieldValidator() {

	}

    public JSR303FieldValidator(Object form, String property) {
        this(form, property, null);
    }

    protected JSR303FieldValidator(Object form, String property, PropertyDescriptor propertyDescriptor) {
        this.form = form;
        this.property = property;
        if (propertyDescriptor == null) {
            BeanDescriptor beanDescriptor = javaxValidator.getConstraintsForClass(form.getClass());
            propertyDescriptor = beanDescriptor.getConstraintsForProperty(property);
        }
        constraintDescriptors = propertyDescriptor.getConstraintDescriptors();
    }

    @Override
    public void validate(Object o) throws InvalidValueException {
        Object value = getValue();
        LOG.debug("validation start: field '{}' - value '{}'", property, value);
        boolean result = true;
        String message = null;
        InvalidValueException invalidValueException = null;

        for (ConstraintDescriptor<?> constraintDescriptor : constraintDescriptors) {

            boolean b = validateByTrialAndError(constraintDescriptor, value);

            // log.info("validation... constraintValidatator: "+
            // constraintValidatator +" - "+b);
            if (!b) {
                Annotation annotation = constraintDescriptor.getAnnotation();
                message = getValidationMessage(annotation, value, constraintDescriptor);
                invalidValueException = new InvalidValueException(message);
                result = false;
                break;
            }

        }
        LOG.debug("validation done: field: {}, value: {}, result: {}, message: {}, invalidValueException: {}",
                new Object[] {property, value, result, message, invalidValueException});
        if (invalidValueException != null) {
            throw invalidValueException;
        }
    }

    /**
     * This method tries to found correct validator by trial and error. At
     * current implementation of hibernate validator there is only two possible
     * validators in that list, so if the first fails, second attempt should
     * pass. But if anyone knows easy way to resolve correct validator based on
     * type this should be replaced.
     *
     * @param constraintDescriptor
     *            describing constraint
     * @return initialized constraint validator for constraintDescriptor's
     *         annotation
     */
    private boolean validateByTrialAndError(ConstraintDescriptor<?> constraintDescriptor, Object value) {

        final List<Class<? extends ConstraintValidator<Annotation, Object>>> constraintValidatorClasses = getValidatorClasses(constraintDescriptor);
        ConstraintValidator<Annotation, Object> constraintValidatator = null;
        for (Class<? extends ConstraintValidator<Annotation, Object>> constraintValidatorClass : constraintValidatorClasses) {

            constraintValidatator = factory.getConstraintValidatorFactory().getInstance(constraintValidatorClass);
            try {
                constraintValidatator.initialize(constraintDescriptor.getAnnotation());
                boolean b = constraintValidatator.isValid(value, ctx);
                return b;
            } catch (ClassCastException ignored) {
                // we just pick next one,
            }
        }
        throw new RuntimeException("no validator found for " + constraintDescriptor.toString());

    }

    @SuppressWarnings("unchecked")
    private List<Class<? extends ConstraintValidator<Annotation, Object>>> getValidatorClasses(
            ConstraintDescriptor constraintDescriptor) {
        return constraintDescriptor.getConstraintValidatorClasses();
    }

    private static String getValidationMessage(Annotation annotation, Object value,
            ConstraintDescriptor<?> constraintDescriptor) {
        try {
            String messageTemplate = (String) annotation.annotationType().getMethod("message").invoke(annotation);
            MessageInterpolatorContext context = new MessageInterpolatorContext(constraintDescriptor, value);
            return messageInterpolator.interpolate(messageTemplate, context, I18N.getLocale());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getValue() {
        java.lang.reflect.Field javaField = ClassUtils.getDeclaredField(form.getClass(), property);
        javaField.setAccessible(true);
        Field vaadinField = null;
        try {
            vaadinField = (Field) javaField.get(form);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return vaadinField.getValue();
    }

    @Override
    public boolean isValid(Object o) {
        try {
            validate(o);
            return true;
        } catch (InvalidValueException e) {
            return false;
        }
    }

    public static void addValidatorsBasedOnAnnotations(Object form) {
        List<java.lang.reflect.Field> javaFields = ClassUtils.getDeclaredFields(form.getClass());
        for (java.lang.reflect.Field javaField : javaFields) {
            javaField.setAccessible(true);
            try {
                Object javaValue = javaField.get(form);
                if (Field.class.isAssignableFrom(javaField.getType()) && javaValue != null) {
                    BeanDescriptor beanDescriptor = javaxValidator.getConstraintsForClass(form.getClass());
                    PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(javaField
                            .getName());
                    if (propertyDescriptor != null) {
                        ((Field) javaValue).addValidator(new JSR303FieldValidator(form, javaField.getName(),
                                propertyDescriptor));

                        // set field required
                        if (javaField.isAnnotationPresent(NotNull.class)) {
                        	Set<ConstraintDescriptor<?>> constraintDescriptors2 = propertyDescriptor.getConstraintDescriptors();
                        	ConstraintDescriptor<?> notNullDescriptor = null;
                            for(ConstraintDescriptor<?> constraintDescriptor : constraintDescriptors2) {
                            	if(constraintDescriptor.getAnnotation() instanceof NotNull) {
                            		notNullDescriptor = constraintDescriptor;
                            		break;
                            	}
                            }

                            ((Field) javaValue).setRequired(true);
                            ((Field) javaValue).setRequiredError(getValidationMessage(javaField.getAnnotation(NotNull.class), null, notNullDescriptor));
                            if(javaValue instanceof AbstractField) {
                            	((AbstractField) javaValue).setValidationVisible(false);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("addValidatorsBasedOnAnnotations failed, field: " + javaField.getName()
                        + ", cause: " + e, e);
            }
        }
    }

	public static MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public static void setMessageInterpolator(
			MessageInterpolator messageInterpolator) {
		JSR303FieldValidator.messageInterpolator = messageInterpolator;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		messageInterpolator = new ResourceBundleMessageInterpolator(new MessageSourceResourceBundleLocator(applicationContext));
	}
}
