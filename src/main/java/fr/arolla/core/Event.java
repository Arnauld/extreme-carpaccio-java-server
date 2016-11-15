package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Event {

    interface Publisher {
        void publish(Event event);
    }

    interface Subscriber {
        void onEvent(Event event);
    }

    interface Bus {
        void subscribe(Subscriber subscriber);
    }
}
