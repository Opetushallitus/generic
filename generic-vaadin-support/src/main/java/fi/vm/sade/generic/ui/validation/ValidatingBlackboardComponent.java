package fi.vm.sade.generic.ui.validation;

import com.github.wolfie.blackboard.Blackboard;

public interface ValidatingBlackboardComponent {

    /**
     * Returns the Blackboard component that holds this form.
     *
     * @return blackboard instance
     */
    public Blackboard getBlackboard();
}
