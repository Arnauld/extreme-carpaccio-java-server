package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class InvalidRegistrationEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final String url;
    private final String username;
    private final int tick;

    public InvalidRegistrationEvent(String username,int tick, String url) {
        this.tick=tick;
        this.url = url;
        this.username=username;
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
