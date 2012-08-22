package fi.vm.sade.generic.ui.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Validator;
import com.vaadin.ui.CustomComponent;

@SuppressWarnings("serial")
public abstract class AbstractValidatingComponent extends CustomComponent implements ValidatingComponent {

    private Collection<Validator> validators = new ArrayList<Validator>();

    public void addValidator(Validator validator) {
        this.validators.add(validator);
    }

    @Override
    public List<Validator.InvalidValueException> getValidationErrors() {
        List<Validator.InvalidValueException> exceptions = new ArrayList<Validator.InvalidValueException>();
        for (Validator validator : validators) {
            try {
                validator.validate(this);
            } catch (Validator.InvalidValueException e) {
                exceptions.add(e);
            }
        }

        return exceptions;
    }
}
