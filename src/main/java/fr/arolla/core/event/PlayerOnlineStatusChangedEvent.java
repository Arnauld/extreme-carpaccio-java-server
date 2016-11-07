package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerOnlineStatusChangedEvent extends TypedEvent implements Event {
    public final int tick;
    public final String username;
    public final boolean online;

    public PlayerOnlineStatusChangedEvent(int tick, String username, boolean online) {
        this.tick = tick;
        this.username = username;
        this.online = online;
    }
}
