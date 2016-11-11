package fr.arolla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Component
public class EventLogger {
    private final Logger eventLog = LoggerFactory.getLogger("fr.arolla.events");
    private final Logger log = LoggerFactory.getLogger(XcServerConfiguration.class);

    private final Event.Bus eventBus;
    private final ObjectMapper mapper;

    public EventLogger(Event.Bus eventBus, ObjectMapper mapper) {
        this.eventBus = eventBus;
        this.mapper = mapper;
    }


    @PostConstruct
    public void logEvents() {
        eventBus.subscribe(this::logEvent);
    }

    private void logEvent(Event e) {
        try {
            eventLog.info("{}", mapper.writeValueAsString(e));
        } catch (JsonProcessingException err) {
            log.warn("Fail to publish event {}", e, err);
        }
    }
}
