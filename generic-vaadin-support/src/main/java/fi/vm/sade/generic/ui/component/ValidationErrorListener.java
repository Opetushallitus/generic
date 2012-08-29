package fi.vm.sade.generic.ui.component;

import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;
import fi.vm.sade.generic.ui.validation.ValidationErrorEvent;

public interface ValidationErrorListener extends Listener {
    @ListenerMethod
    void onError(ValidationErrorEvent event);
}
