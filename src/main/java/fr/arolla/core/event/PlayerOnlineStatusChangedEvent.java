package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerOnlineStatusChangedEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final boolean online;
    private final String username;
    private final int tick;

    public PlayerOnlineStatusChangedEvent(int tick, String username, boolean online) {
        super();
        this.tick=tick;
        this.username=username;
        this.online = online;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTick() {
        return tick;
    }

    public boolean isOnline() {
        return online;
    }
}
