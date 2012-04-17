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

    public ValidationException(String message, Exception cause) {
        super(cause);
        addValidationMessage(message);
    }

    public void addValidationMessage(String msg) {
        validationMessages.add(msg);
    }

    @Override
    public String getMessage() {
        return validationMessages.toString();
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public void setValidationMessages(List<String> validationMessages) {
        this.validationMessages = validationMessages;
    }
}
