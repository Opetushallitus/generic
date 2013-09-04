package fi.vm.sade.generic.ui.message;

import com.vaadin.data.Validator;
import com.vaadin.ui.VerticalLayout;

import fi.vm.sade.generic.ui.validation.ErrorMessage;

public class MessageContainer extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private ErrorMessage errorMessage;
    private InfoMessage infoMessage;
    private ConfirmationMessage confirmationMessage;

    public MessageContainer() {

            errorMessage = new ErrorMessage();
            infoMessage = new InfoMessage();
            confirmationMessage = new ConfirmationMessage();

            addComponent(errorMessage);
            addComponent(infoMessage);
            addComponent(confirmationMessage);

            setWidth(100, UNITS_PERCENTAGE);

    }

    public void addErrorMessage(String message) {
        errorMessage.addError(message);
    }

    public void addErrorMessage(Validator.InvalidValueException e) {
        errorMessage.addError(e);
    }

    public void addInfoMessage(String message) {
        infoMessage.addMessage(message);
    }

    public void addConfirmationMessage(String message) {
        confirmationMessage.addMessage(message);
    }

    public void resetMessages() {
        errorMessage.resetErrors();
        infoMessage.resetMessages();
        confirmationMessage.resetMessages();
    }

    public boolean hasMessages() {
        if(errorMessage.hasErrors() || infoMessage.hasMessages() || confirmationMessage.hasMessages()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasErrorMessages() {
        return errorMessage.hasErrors();
    }

    public boolean hasInfoMessages() {
        return infoMessage.hasMessages();
    }

    public boolean hasConfirmationMessages() {
        return confirmationMessage.hasMessages();
    }

    public ErrorMessage getError() {
        return errorMessage;
    }

    public InfoMessage getInfo() {
        return infoMessage;
    }

    public ConfirmationMessage getConfirmation() {
        return confirmationMessage;
    }
}
