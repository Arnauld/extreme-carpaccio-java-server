package fr.arolla.core;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class InMemoryEventBus implements Event.Publisher, Event.Bus {

    private List<Event.Subscriber> subscribers = new CopyOnWriteArrayList<>();

    @Override
    public void publish(Event event) {
        subscribers.forEach(s -> s.onEvent(event));
    }

    @Override
    public void subscribe(Event.Subscriber subscriber) {
        subscribers.add(subscriber);
    }
}
