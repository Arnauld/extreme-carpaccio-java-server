package fr.arolla.core;

import fr.arolla.core.event.Events;
import fr.arolla.core.event.HasTick;
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


    private List<Event> events=new ArrayList<>(1000);

    @Override
    public List<Event> all() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public List<Event> search(Query query) {
        return events.stream()
                .parallel()
                .filter(query.getTickPredicate())
                .filter(query.getUsernamePredicate())
                .collect(toList());
    }

    @Override
    public void save(Event event) {
        this.events.add(event);
    }
}
