package fr.arolla.core.event;

import fr.arolla.core.Event;
import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionDispatchedEvent extends TypedEvent implements Event {
    public final int tick;
    public final Question q;
    public final String username;

    public QuestionDispatchedEvent(int tick, Question q, String username) {
        this.tick = tick;
        this.q = q;
        this.username = username;
    }
}
