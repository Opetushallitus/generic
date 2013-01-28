/*
 * Copyright (c) 2013 The Finnish Board of Education - Opetushallitus
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


package fi.vm.sade.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An Event that can be sent and received.
 *
 * Category can be used to select "correct" events - see "EventListener" or test case
 * how to configure the persistent message subscriptions with appropriate selectors.
 *
 * @author mlyly
 */
public class Event implements Serializable {

    private Map<String, Serializable> payload = new HashMap<String, Serializable>();

    /**
     * Create event with "default" category.
     */
    public Event() {
        setValue("category", "default");
    }

    /**
     * Create an event with given category.
     *
     * @param category Message category, for example: "Tarjonta", "OrganisaatioModification", "junit", "wateva".
     *                 This value is set to header "JMSType" and used for event listener selectors.
     */
    public Event(String category) {
        setValue("category", category);
    }

    /**
     * Set a value for a key. Note: a special key "category" is set to the jms Message headers,
     * can be used for selectors with persistent subscriptions.
     *
     * @param key
     * @param value if null key is removed
     * @return the event
     */
    public Event setValue(String key, Serializable value) {
        if (value == null) {
            payload.remove(key);
        } else {
            payload.put(key, value);
        }
        return this;
    }

    public Serializable getValue(String key) {
        return payload.get(key);
    }

    /**
     * Message category, for example: "Tarjonta", "OrganisaatioModification", "junit", "wateva"
     *
     * @return
     */
    public String getCategory() {
        return (String) getValue("category");
    }

    @Override
    public String toString() {
        return "Event[category=" + getCategory() + ", payload=" + payload + "]";
    }
}
