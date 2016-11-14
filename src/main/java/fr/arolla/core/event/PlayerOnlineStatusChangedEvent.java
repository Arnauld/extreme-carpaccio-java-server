package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerOnlineStatusChangedEvent extends CarpaccioEvent implements Event,HasUsername {
    public final boolean online;
    private final String username;

    public PlayerOnlineStatusChangedEvent(int tick, String username, boolean online) {
        super(tick);
        this.username=username;
        this.online = online;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
