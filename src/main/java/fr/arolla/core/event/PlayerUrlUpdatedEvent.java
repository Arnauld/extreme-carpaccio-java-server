package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerUrlUpdatedEvent extends TypedEvent implements Event {
    public final String username;
    public final String url;

    public PlayerUrlUpdatedEvent(String username, String url) {
        this.username = username;
        this.url = url;
    }
}
