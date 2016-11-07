package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerRegisteredEvent extends TypedEvent implements Event {
    public final String username;
    public final String url;

    public PlayerRegisteredEvent(String username, String url) {
        this.username = username;
        this.url = url;
    }
}
