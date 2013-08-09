/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.vm.sade.generic.common.I18N;

/**
 * 
 * @author Markus
 * 
 */
public class ErrorPageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorPageHandler.class);
    
    private String messageKey;
    
    
    public void setMessage(String messageKey) {
        this.messageKey = messageKey;
    }
    
    public String getMessage() {
        return I18N.getMessage(messageKey);
    }
    
    public void logError(Throwable t) {
        t.printStackTrace();
        LOG.error("Unexpected error: ", t);
    }
    
}
