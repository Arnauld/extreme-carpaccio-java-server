package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerRegisteredEvent extends TypedEvent implements Event,HasUsername,HasTick {

    private final String url;
    private final String username;
    private final int tick;

    public PlayerRegisteredEvent(String username,int tick, String url) {
        super();
        this.username=username;
        this.url = url;
        this.tick=tick;
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
