package fi.vm.sade.generic.ui.blackboard;

import com.github.wolfie.blackboard.Blackboard;

/**
* @author Antti Salonen
*/
public interface BlackboardProvider {
    Blackboard getBlackboard();
    void setBlackboard(Blackboard blackboard);
}
