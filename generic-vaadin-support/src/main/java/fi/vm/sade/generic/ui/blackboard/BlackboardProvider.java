package fi.vm.sade.generic.ui.blackboard;

import com.github.wolfie.blackboard.Blackboard;

/**
 * Provides blackboard instance to user
 *
* @author Antti Salonen
*/
public interface BlackboardProvider {

    /**
     * @return blackboard instance bound for example to threadlocal
     */
    Blackboard getBlackboard();

    /**
     * Set blackboard instance bound for example to threadlocal
     * @param blackboard
     */
    void setBlackboard(Blackboard blackboard);
}
