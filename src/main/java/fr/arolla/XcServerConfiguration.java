package fr.arolla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.arolla.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Configuration
public class XcServerConfiguration {
    private final Logger eventLog = LoggerFactory.getLogger("fr.arolla.events");
    private final Logger log = LoggerFactory.getLogger(XcServerConfiguration.class);

    private final Event.Bus eventBus;
    private final ObjectMapper mapper;

    @Autowired
    public XcServerConfiguration(Event.Bus eventBus, ObjectMapper mapper) {
        this.eventBus = eventBus;
        this.mapper = mapper;
    }

    @PostConstruct
    public void configureMapper() {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
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
