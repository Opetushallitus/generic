/**
 * 
 */
package fi.vm.sade.generic.ui.validation;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author tommiha
 * 
 */
public class ErrorMessage extends CustomComponent {

    private static final long serialVersionUID = -6495162299981290991L;

    private boolean hasErrors = false;

    // Tapsa sanoi, että pitää laittaa näin.
    private VerticalLayout mainLayout = new VerticalLayout();

    public ErrorMessage() {
        setCompositionRoot(mainLayout);
        addStyleName("error-container");
    }

    public ErrorMessage(InvalidValueException e) {
        addError(e);
    }

    public void addError(InvalidValueException e) {
        if (e.getCauses() == null) {
            addError(e.getMessage());
        } else {
            for (InvalidValueException causes : e.getCauses()) {
                addError(causes.getMessage());
            }
        }
    }

    public void addError(String error) {
        Label errorLabel = new Label(error);
        errorLabel.addStyleName("error");
        mainLayout.addComponent(errorLabel);
        hasErrors = true;
    }

    public void resetErrors() {
        hasErrors = false;
        mainLayout.removeAllComponents();
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
