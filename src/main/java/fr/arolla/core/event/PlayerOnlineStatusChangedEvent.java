package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerOnlineStatusChangedEvent extends IdentifiableEvent implements Event {
    public final boolean online;

    public PlayerOnlineStatusChangedEvent(int tick, String username, boolean online) {
        super(username, tick);
        this.online = online;
    }
}
