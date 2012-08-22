package fi.vm.sade.generic.ui.validation;

import java.util.Collection;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;

/**
 * @author jukka
 * @version 8/6/1212:05 PM}
 * @since 1.1
 */
public interface ValidatingComponent {
    Field[] getFields();

    String getGroupLabel();

    Collection<InvalidValueException> getValidationErrors();
    
    void validate() throws com.vaadin.data.Validator.InvalidValueException;
}
