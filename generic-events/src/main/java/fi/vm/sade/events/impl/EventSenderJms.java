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
import fi.vm.sade.events.EventSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Implement EventSender via JMS.
 * <p/>
 * Example configuration:
 * <pre>
 * <bean id="eventsJmsFactory" class="org.apache.activemq.pool.PooledConnectionFactory" destroy-method="stop">
 *   <property name="connectionFactory">
 *     <bean class="org.apache.activemq.ActiveMQConnectionFactory">
 *       <property name="brokerURL">
 *         <value>${activemq.events.brokerurl}</value>
 *       </property>
 *     </bean>
 *   </property>
 * </bean>
 *
 * -- Define the target topic where events will be sent
 *
 * <bean id="eventsDestination" class="org.apache.activemq.command.ActiveMQTopic">
 *   <constructor-arg value="${activemq.topic.name.events}"/>
 * </bean>
 *
 * <bean id="eventsJmsTemplate" class="org.springframework.jms.core.JmsTemplate">
 *   <property name="connectionFactory">
 *     <ref local="eventsJmsFactory"/>
 *   </property>
 *   <property name="defaultDestination" ref="eventsDestination"/>
 * </bean>
 *
 * <bean id="eventsSender" class="fi.vm.sade.events.impl.EventSenderJms">
 *   <property name="jmsTemplate" ref="eventsJmsTemplate"/>
 * </bean>
 * </pre>
 *
 * @author mlyly
 */
public class EventSenderJms implements ApplicationContextAware, EventSender {

    private static final Logger LOG = LoggerFactory.getLogger(EventSenderJms.class);
    private ApplicationContext applicationContext;
    private JmsTemplate jmsTemplate;
    private boolean selfInit = false;

    @Override
    public void sendEvent(final Event event) {
        LOG.info("sendEvent({})", event);

        // Initialize from default configuration if not configured from outside (ie. new EventSenderJms() called)
        if (applicationContext == null) {
            LOG.info("EventSenderJms.sendEvent() - self initialization from default configuration.");

            selfInit = true;
            applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/events-context.xml");
            jmsTemplate = (JmsTemplate) applicationContext.getBean("eventsJmsTemplate");
        }

        // Send message with event category as property (used in selectors)

        // jmsTemplate.convertAndSend(event);
        jmsTemplate.convertAndSend(event, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws JMSException {
                message.setStringProperty("category", event.getCategory());
                return message;
            }
        });

        // Cleanup
        if (selfInit) {
            ((ClassPathXmlApplicationContext) applicationContext).close();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOG.info("setApplicationContext()");
        this.applicationContext = applicationContext;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        LOG.info("setJmsTemplate()");
        this.jmsTemplate = jmsTemplate;
    }
}
