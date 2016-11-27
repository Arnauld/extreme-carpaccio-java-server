package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerQuestionSkippedEvent extends TypedEvent implements Event, HasUsername, HasTick {
    private final String username;
    private final int tick;

    public PlayerQuestionSkippedEvent(int tick, String username) {
        super();
        this.tick = tick;
        this.username = username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTick() {
        return tick;
    }
}
