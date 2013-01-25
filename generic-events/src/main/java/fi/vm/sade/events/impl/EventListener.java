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

package fi.vm.sade.events.impl;

import fi.vm.sade.events.Event;
import fi.vm.sade.events.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.ArrayList;

/**
 * Event listener is used to receive the EventSenders sent messages.
 * <p/>
 * When it receives a JMS message it will extract the "Event" and call
 * all registered EventHandler-instances.
 *
 * @author mlyly
 */
public class EventListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);

    private ArrayList<EventHandler> eventHandlers = new ArrayList<EventHandler>();

    /**
     * Add new event handler.
     *
     * @param eventHandler
     */
    public void addEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    /**
     * Remove an event handler.
     *
     * @param eventHandler
     * @return true if removed
     */
    public boolean removeEventHandler(EventHandler eventHandler) {
        return eventHandlers.remove(eventHandler);
    }

    @Override
    public void onMessage(Message message) {
        LOG.info("onMessage()...");

        // Do we actually have any handlers?
        if (!eventHandlers.isEmpty()) {
            // Yes, we do
            Event event = null;

            // Extract the event
            if (message instanceof ObjectMessage) {
                try {
                    event = (Event) ((ObjectMessage) message).getObject();
                } catch (JMSException e) {
                    // Bummer...
                    LOG.error("Failed to extract Event from message... " + message, e);
                }
            }

            // TODO messages in JSON / XML format
            if (message instanceof TextMessage) {
                LOG.warn("EventListener.onMessage() -- TextMessage's not supported yet. Discarding event: {}", event);
                event = null;
            }

            // Process the event with the handler(s)
            if (event != null) {
                for (EventHandler eventHandler : eventHandlers) {
                    try {
                        eventHandler.handleEvent(event);
                    } catch (Throwable ex) {
                        LOG.info("Event handler failed: " + ex);
                    }
                }
            }
        } else {
            LOG.warn("EventListener.onMessage() -- No evet handlers registered! Discarding event: {}", message);
        }

        LOG.info("onMessage()... done.");
    }
}
