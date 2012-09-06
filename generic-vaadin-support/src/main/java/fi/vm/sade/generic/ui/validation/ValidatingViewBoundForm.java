package fi.vm.sade.generic.ui.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.vaadin.addon.formbinder.ViewBoundForm;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

/**
 * @author jukka
 * @version 8/6/1211:55 AM}
 * @since 1.1
 */
public class ValidatingViewBoundForm extends ViewBoundForm {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5199327413366176132L;

    public ValidatingViewBoundForm(ComponentContainer form) {
        super(form);
    }

    @Override
    public void validate() throws Validator.InvalidValueException {
        try {
			super.validate();
			// It's already valid.
			return;
		} catch (Validator.InvalidValueException e) {
			// Exception thrown, create the list.
		}

        List<InvalidValueException> errors = new ArrayList<Validator.InvalidValueException>();
        
        for (final Iterator<?> i = getItemPropertyIds().iterator(); i.hasNext();) {
        	Object itemPropertyId = i.next();
        	Field field = getField(itemPropertyId);
        	
        	if(field instanceof AbstractComponent) {
				((AbstractComponent) field).setComponentError(null);
        	}
        	
            try {
				field.validate();
            } catch (EmptyValueException e) {
            	if(field instanceof AbstractField) {
            		((AbstractField) field).setValidationVisible(true);
            	}
            	errors.add(e);
            } catch (InvalidValueException e) {
				if(field instanceof AbstractComponent && field.getRequiredError() == null) {
					((AbstractComponent) field).setComponentError(new UserError(e.getMessage()));
				}
				errors.add(e);
			}
        }
        
        if(!errors.isEmpty()) {
        	throw new InvalidValueException(getRequiredError(), errors.toArray(new InvalidValueException[0]));
        }
    }
}
