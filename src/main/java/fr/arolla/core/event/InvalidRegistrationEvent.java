package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class InvalidRegistrationEvent extends CarpaccioEvent implements Event,HasUsername {
    public final String url;
    private String username;

    public InvalidRegistrationEvent(String username,int tick, String url) {
        super(tick);
        this.url = url;
        this.username=username;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
