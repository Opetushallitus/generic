package fi.vm.sade.generic.ui.app;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;
import fi.vm.sade.generic.ui.blackboard.BlackboardContext;

/**
 * @author Antti Salonen
 */
public abstract class AbstractBlackboardSadeApplication extends AbstractSadeApplication implements ApplicationContext.TransactionListener {

    private Blackboard blackboardInstance = new Blackboard();

    @Override
    public synchronized void init() {

        //Init blackboard event bus
        registerListeners(blackboardInstance);

        // set blackboard to threadlocal - NOTE! tehdään transactionStartissa joten tästä pois
        //setBlackboard(blackboardInstance);

        super.init();

        // At every "transaction" start set the threadlocal blackboard instance
        getContext().addTransactionListener(this);
    }

    /**
     * Invoked at init to register event listeners and events with given event bus.
     *
     * @param blackboard
     */
    protected abstract void registerListeners(Blackboard blackboard);

    /**
     * Get ThreadLocal Blackboard instance
     * @return
     */
    public static Blackboard getBlackboard() { // TODO: pois?
        return BlackboardContext.getBlackboard();
    }

    /*
    * Implement TransactionListener interface
    */
    @Override
    public void transactionStart(Application application, Object transactionData) {
        if (application == this) {
            BlackboardContext.setBlackboard(blackboardInstance);
        }
    }

    @Override
    public void transactionEnd(Application application, Object transactionData) {
        if (application == this) {
            BlackboardContext.setBlackboard(null);
        }
    }

}
