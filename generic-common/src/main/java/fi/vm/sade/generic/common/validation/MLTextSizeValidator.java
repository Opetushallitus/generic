package fi.vm.sade.generic.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * javavax.validation Validator for MultiLingualText annotated with @MLSize
 *
 * mostly copypaste from hibernate's SizeValidatorForString
 *
 * @author Antti Salonen
 */
public class MLTextSizeValidator implements ConstraintValidator<MLTextSize, MultiLingualText> {

    private int min, max;
    private MLTextSize size;

    @Override
    public void initialize(MLTextSize parameters) {
        size = parameters;
        min = parameters.min();
        max = parameters.max();
        validateParameters();
    }

    public boolean isValid(MultiLingualText mltext, ConstraintValidatorContext constraintValidatorContext) {
        // check that every field isn't null
        if (mltext == null || mltext.allAreNull()) {
            return !size.oneRequired();
        }

        // proceed with actual validation, validate each field, all must be valid (null counts as valid ONLY if allRequired is false)
        boolean fi = isValid(mltext.getTextFi(), constraintValidatorContext);
        boolean sv = isValid(mltext.getTextSv(), constraintValidatorContext);
        boolean en = isValid(mltext.getTextEn(), constraintValidatorContext);
        return fi & sv && en;
    }

    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return !size.allRequired();
        } else {
            int length = s.length();
            return length >= min && length <= max;
        }
    }

    private void validateParameters() {
        if (min < 0) {
            throw new IllegalArgumentException("The min parameter cannot be negative.");
        }
        if (max < 0) {
            throw new IllegalArgumentException("The max parameter cannot be negative.");
        }
        if (max < min) {
            throw new IllegalArgumentException("The length cannot be negative.");
        } else {
            return;
        }
    }

}
