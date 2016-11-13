package fr.arolla.core.event;

import fr.arolla.core.Event;
import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionDispatchedEvent extends IdentifiableEvent implements Event {
    public final Question q;

    public QuestionDispatchedEvent(int tick, Question q, String username) {
        super(username, tick);
        this.q = q;
    }
}
