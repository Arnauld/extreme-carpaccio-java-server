package fr.arolla.core.event;

import fr.arolla.core.Event;
import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionDispatchedEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final Question question;
    private final String username;
    private final int tick;

    public QuestionDispatchedEvent(int tick, Question question, String username) {
        super();
        this.tick=tick;
        this.username=username;
        this.question = question;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTick() {
        return tick;
    }

    public Question getQuestion() {
        return question;
    }
}
