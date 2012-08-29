package fi.vm.sade.generic.ui.validation;

import java.util.ArrayList;
import java.util.Collection;

import com.vaadin.data.Buffered;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import org.vaadin.addon.formbinder.ViewBoundForm;

import com.vaadin.data.Validator;
import com.vaadin.ui.ComponentContainer;

/**
 * @author jukka
 * @version 8/6/1211:55 AM}
 * @since 1.1
 */
public class ValidatingViewBoundForm extends ViewBoundForm {

    private Collection<ValidatingComponent> validatorList = new ArrayList<ValidatingComponent>();

    public ValidatingViewBoundForm(ComponentContainer form) {
        super(form);

        if (form instanceof ValidatingForm) {
            validatorList = ((ValidatingForm) form).getValidatingComponents();
        }

    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        BlackboardContext.getBlackboard().fire(new ClearValidationErrorsEvent());
        super.commit();
    }

    @Override
    public void validate() throws Validator.InvalidValueException {
        super.validate();

        for (ValidatingComponent validator : validatorList) {
            validator.validate();
        }
    }

    @Override
    public boolean isValid() {
        boolean valid = true;

        for (ValidatingComponent validatingComponent : validatorList) {
            try {
                validatingComponent.validate();
            } catch (Validator.InvalidValueException e) {
                valid &= false;
            }
        }
        return super.isValid() && valid;
    }
}
