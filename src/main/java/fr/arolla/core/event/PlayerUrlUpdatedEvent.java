package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerUrlUpdatedEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final String url;
    private final String username;
    private final int tick;

    public PlayerUrlUpdatedEvent(String username,int tick, String url) {
        super();
        this.tick=tick;
        this.username=username;
        this.url = url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTick() {
        return tick;
    }

    public String getUrl() {
        return url;
    }
}
