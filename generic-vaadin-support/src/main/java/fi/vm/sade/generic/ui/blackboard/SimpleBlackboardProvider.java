package fi.vm.sade.generic.ui.blackboard;

import com.github.wolfie.blackboard.Blackboard;

/**
* @author Antti
*/
public class SimpleBlackboardProvider implements BlackboardProvider {
    private Blackboard blackboard = new Blackboard();

    @Override
    public Blackboard getBlackboard() {
        return blackboard;
    }

    @Override
    public void setBlackboard(Blackboard blackboard) {
        this.blackboard = blackboard;
    }
}
