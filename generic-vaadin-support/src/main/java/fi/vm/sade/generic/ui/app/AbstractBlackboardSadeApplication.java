/*
 *
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.generic.ui.app;

import com.github.wolfie.blackboard.Blackboard;
import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;

import fi.vm.sade.generic.ui.blackboard.BlackboardContext;

/**
 * Super class for sade vaadin based applications which use Blackboard. With
 * this superclass Blackboard can be accessed always via
 * BlackboardContext.getBlackboard(). This class handles blackboard binding and
 * releasing.
 *
 * @see BlackboardContext
 * @see fi.vm.sade.generic.ui.blackboard.BlackboardProvider
 * @author Antti Salonen
 */
public abstract class AbstractBlackboardSadeApplication extends AbstractSadeApplication implements
        ApplicationContext.TransactionListener {

    private static final long serialVersionUID = 1L;

    private Blackboard blackboardInstance = new Blackboard();

    @Override
    public void init() {

        // Init blackboard event bus
        registerListeners(blackboardInstance);

        // At every "transaction" start set the threadlocal blackboard instance
        getContext().addTransactionListener(this);

        // set blackboard to threadlocal also for first request
        BlackboardContext.setBlackboard(blackboardInstance);

        super.init();
    }

    /**
     * Invoked at init to register event listeners and events with given event
     * bus.
     *
     * @param blackboard
     */
    protected abstract void registerListeners(Blackboard blackboard);

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
