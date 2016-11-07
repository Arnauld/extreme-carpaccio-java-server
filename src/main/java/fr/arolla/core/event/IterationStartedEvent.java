package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class IterationStartedEvent extends TypedEvent implements Event {
    public final int tick;

    public IterationStartedEvent(int tick) {
        this.tick = tick;
    }
}
