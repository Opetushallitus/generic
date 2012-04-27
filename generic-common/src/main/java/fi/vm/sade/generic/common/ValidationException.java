package fi.vm.sade.generic.common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Antti
 */
public class ValidationException extends LocalizedBusinessException {

    public static final String KEY = "validation.exception";
    private List<String> validationMessages = new ArrayList<String>();

    public ValidationException() {
        super(KEY);
    }

    public ValidationException(String message) {
        super(KEY);
        addValidationMessage(message);
    }

    public ValidationException(String message, Exception cause) {
        super(cause, KEY);
        addValidationMessage(message);
    }

    public ValidationException(String message, String key) {
        super(message, key);
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
