
# Generic :: generic-events

A simplish event system to send and receive events.


## Configuration

1. Add the maven depenency to your pom.xml:
        <dependency>
            <groupId>fi.vm.sade.generic</groupId>
            <artifactId>generic-events</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

2. Make sure following properties is set in your .properties configuration
        # For example
        activemq.brokerurl=tcp://localhost:61616
        # Also you can define other that default event topic name
        # activemq.topic.name.events:fi.vm.sade.events.eventsTopic

3. In spring context, include context
    <!-- Get configuration for generic/generic-events system -->
    <import resource="classpath:META-INF/spring/events-context.xml" />


## How to send Events

(see: EventServerJmsTest.java)

Autowire or get the bean:

    @Autowired(required = true)
    private EventSender eventSender;

And send events:

            Event e = new Event("Tarjonta");
            e.addValue("foo", "bar")
                    .addValue("myId", 123456L)
                    .addValue("date", new Date());
            eventSender.sendEvent(e);


## How to receive events

Configure what events you want to receive in spring:

    <!--
      Register durable subscription to receive events, notice clientId AND durableSubscriptionName
    -->
    <bean id="eventsMessageContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
        <property name="connectionFactory" ref="eventsJmsFactory"/>     <!-- defined in "events-context.xml" -->
        <property name="destination" ref="eventsDestination"/>          <!-- defined in "events-context.xml" -->
        <property name="messageListener" ref="eventsMessageListener"/>  <!-- defined in "events-context.xml" -->

        <property name="concurrentConsumers" value="1"/>
        <property name="sessionTransacted" value="true"/>
        <property name="pubSubDomain" value="true"/>
        <property name="subscriptionDurable" value="true"/>

        <property name="clientId" value="generic-events-junit"/>
        <property name="durableSubscriptionName" value="events"/>
        <property name="messageSelector" value="category = 'test' OR category = 'junit'"/>
    </bean>


If you don't need durable messaging use something like this:

    <bean id="eventsMessageContainer" class="org.springframework.jms.listener.DefaultMessageListenerContainer">
        <property name="connectionFactory" ref="eventsJmsFactory"/>     <!-- defined in "events-context.xml" -->
        <property name="destination" ref="eventsDestination"/>          <!-- defined in "events-context.xml" -->
        <property name="messageListener" ref="eventsMessageListener"/>  <!-- defined in "events-context.xml" -->
    </bean>


Autowire or get bean:

    @Autowired(required = true)
    private EventListener eventListener;


Add handler to process events:

        // Register event handler
        EventHandler eventHandler = new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                LOG.info("*** handleEvent! {}", event);
            }
        };
        eventListener.addEventHandler(eventHandler);
