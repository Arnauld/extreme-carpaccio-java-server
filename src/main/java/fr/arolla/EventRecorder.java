package fr.arolla;

import fr.arolla.core.Event;
import fr.arolla.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
public class EventRecorder {

    private final Logger log = LoggerFactory.getLogger(XcServerConfiguration.class);

    private final Event.Bus eventBus;
    private final Events events;

    public EventRecorder(Event.Bus eventBus, Events events) {
        this.eventBus = eventBus;
        this.events = events;
    }



    @PostConstruct
    public void storeEvents() {
        eventBus.subscribe(this::storeEvent);
    }

    private void storeEvent(Event event) {
        try {
            events.save(event);
        } catch(Exception e){
            log.warn("Fail to save event {}", event, e);
        }
    }
}
