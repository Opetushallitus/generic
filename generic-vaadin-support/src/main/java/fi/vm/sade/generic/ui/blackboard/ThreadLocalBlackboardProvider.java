package fi.vm.sade.generic.ui.blackboard;

import com.github.wolfie.blackboard.Blackboard;

/**
* @author Antti
*/
public class ThreadLocalBlackboardProvider implements BlackboardProvider {

    private ThreadLocal<Blackboard> blackboard = new ThreadLocal<Blackboard>();

    @Override
    public Blackboard getBlackboard() {
        return blackboard.get();
    }

    @Override
    public void setBlackboard(Blackboard blackboard) {
        this.blackboard.set(blackboard);
    }
}
