package fr.arolla.core.event;

import fr.arolla.core.Event;
import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionDispatchedEvent extends CarpaccioEvent implements Event,HasUsername {
    public final Question q;
    private final String username;

    public QuestionDispatchedEvent(int tick, Question q, String username) {
        super(tick);
        this.username=username;
        this.q = q;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
