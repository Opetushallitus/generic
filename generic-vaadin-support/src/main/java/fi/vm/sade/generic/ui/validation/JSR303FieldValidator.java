package fi.vm.sade.generic.ui.validation;

import com.vaadin.data.Validator;
import com.vaadin.ui.Field;
import fi.vm.sade.generic.common.ClassUtils;
import fi.vm.sade.generic.common.validation.MLTextSize;
import fi.vm.sade.generic.common.validation.MLTextSizeValidator;
import org.hibernate.validator.constraints.impl.NotNullValidator;
import org.hibernate.validator.constraints.impl.PatternValidator;
import org.hibernate.validator.constraints.impl.SizeValidatorForString;
import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

/**
 * Vaadin validator that validates based on JSR-303 annotations given to form's Vaadin fields.
 * Traditionally JSR-303 annotations are given to domain class properties, but with this validator
 * it is able to annotate the form fields.
 *
 * Usage:
 * - Annotate form's vaadin fields with JSR-303 annotations
 * - Call JSR303FieldValidator.addValidatorsBasedOnAnnotations(form)
 *
 * HINT:
 * - You can use same annotations in corresponding JPA model class for service side validation
 *
 * Supports following JSR-303 annotations:
 *  - NotNull, Size, Pattern, MLTextSize
 *
 * TODO: support for more annotations
 *
 * Example:
 *
 * class SampleForm {
 *     @NotNull
 *     @Size(min = 3, max = 100)
 *     private TextField nameField = ...;
 *
 *     SampleForm() {
 *          ...
 *          JSR303FieldValidator.addValidatorsBasedOnAnnotations(this);
 *          ...
 *     }
 * }
 *
 * class SampleJPA {
 *     @NotNull
 *     @Size(min = 3, max = 100)
 *     private String name;
 * }
 *
 * @author Antti Salonen
 */
class JSR303FieldValidator implements Validator {

    private static final Logger log = LoggerFactory.getLogger(JSR303FieldValidator.class);
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static javax.validation.Validator javaxValidator = factory.getValidator();
    private static MessageInterpolator messageInterpolator = factory.getMessageInterpolator();

    private Set<ConstraintDescriptor<?>> constraintDescriptors;
    private ConstraintValidatorContext ctx = null; // NOTE: tätä ei oikein saa helposti(?) mutta validaattorit ei tätä oikeastaan näytä tarvitsevan

    private Object form;
    private String property;

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
        //log.info("validation start, field: "+ property +", value: "+value);
        boolean result = true;
        String message = null;
        InvalidValueException invalidValueException = null;


        for (ConstraintDescriptor constraintDescriptor : constraintDescriptors) {
            Annotation annotation = constraintDescriptor.getAnnotation();
            ConstraintValidator constraintValidatator = getValidator(annotation);
            boolean b = constraintValidatator.isValid(value, ctx);
            //log.info("validation... constraintValidatator: "+ constraintValidatator +" - "+b);
            if (!b) {
                message = getValidationMessage(annotation, value, constraintDescriptor);
                invalidValueException = new InvalidValueException(message);
                result = false;
                break;
            }
        }
        log.info("validation done, field: "+property+", value: "+value+", result: "+result+", message: "+message+", invalidValueException: "+invalidValueException);
        if (invalidValueException != null) {
            throw invalidValueException;
        }
    }

    private String getValidationMessage(Annotation annotation, Object value, ConstraintDescriptor<?> constraintDescriptor) {
        try {
            String messageTemplate = (String) annotation.annotationType().getMethod("message").invoke(annotation);
            MessageInterpolatorContext context = new MessageInterpolatorContext(constraintDescriptor, value);
            return messageInterpolator.interpolate(messageTemplate, context);
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

    private ConstraintValidator getValidator(Annotation annotation) {

        // NOTE: tässäkin voisi käyttää hibernatevalidatoria jos osaisi kaivaa oikean paikan mutta ei noista tarvita kuin muutamaa

        ConstraintValidator validator = null;
        if (annotation instanceof NotNull) {
            validator = new NotNullValidator();
        } else if (annotation instanceof MLTextSize) {
            validator = new MLTextSizeValidator();
        } else if (annotation instanceof Size) {
            validator = new SizeValidatorForString();
        } else if (annotation instanceof Pattern) {
            validator = new PatternValidator();
        }

        if (validator != null) {
            validator.initialize(annotation);
        }
        return validator;
    }

    public static void addValidatorsBasedOnAnnotations(Object form) {
        List<java.lang.reflect.Field> javaFields = ClassUtils.getDeclaredFields(form.getClass());
        for (java.lang.reflect.Field javaField : javaFields) {
            javaField.setAccessible(true);
            try {
                Object javaValue = javaField.get(form);
                if (Field.class.isAssignableFrom(javaField.getType()) && javaValue != null) {
                    BeanDescriptor beanDescriptor = javaxValidator.getConstraintsForClass(form.getClass());
                    PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(javaField.getName());
                    if (propertyDescriptor != null) {
                        ((Field)javaValue).addValidator(new JSR303FieldValidator(form, javaField.getName(), propertyDescriptor));

                        // set field required
                        if (javaField.isAnnotationPresent(NotNull.class)) {
                            ((Field)javaValue).setRequired(true);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("addValidatorsBasedOnAnnotations failed, field: "+ javaField.getName()+", cause: " + e, e);
            }
        }
    }
}
