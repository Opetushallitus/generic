package fi.vm.sade.generic.ui.component;

import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.annotation.ListenerMethod;
import fi.vm.sade.generic.ui.validation.ClearValidationErrorsEvent;

public interface ClearValidationErrorsListener extends Listener {

    @ListenerMethod
    void onClear(ClearValidationErrorsEvent event);
}
