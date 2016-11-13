package fr.arolla.core;

import fr.arolla.core.event.Events;
import fr.arolla.core.event.IdentifiableEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by fmaury on 13/11/16.
 */

@Component
public class InMemoryEventStorage implements Events {


    private List<IdentifiableEvent> events=new ArrayList<>(1000);

    @Override
    public List<IdentifiableEvent> all() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public List<IdentifiableEvent> search(EventQuery query) {
        return events.stream()
                .parallel()
                .filter(query.getTickPredicate())
                .filter(query.getUsernamePredicate())
                .collect(toList());
    }

    @Override
    public void save(IdentifiableEvent event) {
        this.events.add(event);
    }
}
