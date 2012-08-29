package fi.vm.sade.generic.ui.validation;

import com.github.wolfie.blackboard.Event;

public class ValidationErrorEvent implements Event {
    private ValidatingBlackboardComponent component;
    private String property;
    private String message;

    public ValidationErrorEvent(ValidatingBlackboardComponent component, String property, String message) {
        this.component = component;
        this.property = property;
        this.message = message;
    }

    public ValidatingBlackboardComponent getComponent() {
        return component;
    }

    public String getProperty() {
        return property;
    }

    public String getMessage() {
        return message;
    }
}
