package fi.vm.sade.generic.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antti
 */
public class ValidationException extends Exception {

    private List<String> validationMessages = new ArrayList<String>();

    public ValidationException() {
    }

    public ValidationException(String message) {
        addValidationMessage(message);
    }

    public void addValidationMessage(String msg) {
        validationMessages.add(msg);
    }

    @Override
    public String getMessage() {
        return validationMessages.toString();
    }
}
