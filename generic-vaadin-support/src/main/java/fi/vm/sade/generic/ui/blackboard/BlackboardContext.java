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
package fi.vm.sade.generic.ui.blackboard;

import com.github.wolfie.blackboard.Blackboard;

/**
 * Context for getting blackboard, Uses BlackboardProvider inside.
 * In Application we should use ThreadLocalBlackboardProvider (default).
 * When used together with AbstractBlackboardSadeApplication,
 * this combination will init blackboard when application starts, and set/unset blackboard to threadlocal when request starts/ends.
 *
 * In component based selenium tests we should use SimpleBlackboardProvider, which gives same blackboard instance in every situation.
 *
 * @author Antti Salonen
 */
public class BlackboardContext {

    private static BlackboardProvider blackboardProvider = new ThreadLocalBlackboardProvider();

    public static void setBlackboardProvider(BlackboardProvider blackboardProvider) {
        BlackboardContext.blackboardProvider = blackboardProvider;
    }

    public static Blackboard getBlackboard() {
        if (blackboardProvider == null) {
            throw new NullPointerException("BlackboardContext.blackboardProvider not initialized");
        }
        return blackboardProvider.getBlackboard();
    }

    public static void setBlackboard(Blackboard blackboard) {
        if (blackboardProvider == null) {
            throw new NullPointerException("BlackboardContext.blackboardProvider not initialized");
        }
        blackboardProvider.setBlackboard(blackboard);
    }

}
