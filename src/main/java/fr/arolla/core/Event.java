package fr.arolla.core;

import java.io.Serializable;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Event extends Serializable {

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
