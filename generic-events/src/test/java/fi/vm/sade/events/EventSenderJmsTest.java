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

import fi.vm.sade.events.impl.EventListener;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for Event sending and receiving.
 *
 * @author mlyly
 */
@ContextConfiguration(locations = {
        "classpath:test-context.xml"
})
@RunWith(SpringJUnit4ClassRunner.class)
public class EventSenderJmsTest {

    private static final Logger LOG = LoggerFactory.getLogger(EventSenderJmsTest.class);
    int messageReceivedCounter = 0;
    int messageSentCounter = 0;
    @Autowired(required = true)
    private EventSender eventSender;
    @Autowired(required = false)
    private EventListener eventListener;

    // Ignored - used to test persistent subscriptions and delivery by defined category.
    @Ignore
    @Test
    public void testSend() {
        LOG.info("testSend()...");

        // Send events
        for (int i = 0; i < 10; i++) {
            Event e = new Event("test");
            e.setValue("foo", "bar")
                    .setValue("myId", i)
                    .setValue("date", new Date());

            eventSender.sendEvent(e);
            messageSentCounter++;
        }

        LOG.info("testSend()... done.");
    }

    // Ignored - used to test persistent subscriptions and delivery by defined category.
    @Ignore
    @Test
    public void testReceive() throws Exception {
        LOG.info("testReceive()...");

        // Register event handler
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                LOG.info("*** handleEvent! {}", event);
                messageReceivedCounter++;
            }
        };
        eventListener.addEventHandler(eventHandler);

        LOG.info(" sleep...");
        Thread.sleep(5000L);

        LOG.info("testReceive()... done.");
    }

//    @Ignore
    @Test
    public void testSendSignal() throws Exception {
        LOG.info("testSendSignal()...");

        // Register event handler
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                LOG.info("*** handleEvent! {}", event);
                messageReceivedCounter++;
            }
        };
        eventListener.addEventHandler(eventHandler);

        // Send events
        for (int i = 0; i < 10; i++) {
            Event e = new Event("junit");
            e.setValue("foo", "bar")
                    .setValue("myId", i);

            eventSender.sendEvent(e);
            messageSentCounter++;
        }

        // Wait to be sure delivered
        LOG.info("sent messages... waiting...");
        Thread.sleep(1000L);

        // Unregister event handler
        eventListener.removeEventHandler(eventHandler);

        // Success?
        assertEquals(messageSentCounter, messageReceivedCounter);

        LOG.info("testSendSignal()... done.");
    }
}
