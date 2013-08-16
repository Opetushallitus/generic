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
    private String toFrontPageKey;
    private String stamp;
    
    
    public void setMessage(String messageKey) {
        this.messageKey = messageKey;
        stamp = String.format("%s", System.currentTimeMillis());
    }
    
    public String getMessage() {
        return I18N.getMessage(messageKey, stamp);
    }
    
    public void logError(Throwable t) {
        LOG.error("Unexpected error, code: " + stamp, t);
    }

    public String getToFrontPage() {
        return I18N.getMessage(toFrontPageKey);
    }

    public void setToFrontPage(String toFrontPageKey) {
        this.toFrontPageKey = toFrontPageKey;
    }
    
}
