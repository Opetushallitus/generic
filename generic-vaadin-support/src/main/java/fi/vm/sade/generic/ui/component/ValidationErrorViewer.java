package fi.vm.sade.generic.ui.component;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;
import fi.vm.sade.generic.ui.validation.ClearValidationErrorsEvent;
import fi.vm.sade.generic.ui.validation.ValidationErrorEvent;

public class ValidationErrorViewer extends CustomComponent implements ValidationErrorListener, ClearValidationErrorsListener {

    private final Panel panel;

    public ValidationErrorViewer() {
        panel = new Panel("Error");
        setCompositionRoot(panel);
        setVisible(false);
        BlackboardContext.getBlackboard().addListener(this);
    }

    @Override
    public void onError(ValidationErrorEvent event) {
        this.panel.addComponent(new Label("Validation error: " + event.getMessage()));
        setVisible(true);
    }

    @Override
    public void onClear(ClearValidationErrorsEvent event) {
        this.panel.setVisible(false);
        this.panel.removeAllComponents();
    }

}
