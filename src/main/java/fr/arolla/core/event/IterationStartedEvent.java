package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class IterationStartedEvent extends TypedEvent implements Event,HasTick {

    private final int tick;

    public IterationStartedEvent(int tick) {
        super();
        this.tick=tick;
    }

    @Override
    public int getTick() {
        return tick;
    }
}
