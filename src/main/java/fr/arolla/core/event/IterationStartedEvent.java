package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class IterationStartedEvent extends CarpaccioEvent implements Event {

    public IterationStartedEvent(int tick) {
        super(tick);
    }
}
