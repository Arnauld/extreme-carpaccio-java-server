package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerUrlUpdatedEvent extends CarpaccioEvent implements Event,HasUsername {
    public final String url;
    private final String username;

    public PlayerUrlUpdatedEvent(String username,int tick, String url) {
        super(tick);
        this.username=username;
        this.url = url;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
