package fr.arolla.core;

import fr.arolla.core.event.IterationStartedEvent;
import fr.arolla.core.event.PlayerCashLostEvent;
import fr.arolla.core.event.PlayerCashWonEvent;
import fr.arolla.core.event.PlayerOnlineStatusChangedEvent;
import fr.arolla.core.event.QuestionDispatchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Component
public class EventPublishingGameListener implements GameListener {
    private final Event.Publisher publisher;

    @Autowired
    public EventPublishingGameListener(Event.Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void iterationStarting(int tick) {
        publisher.publish(new IterationStartedEvent(tick));
    }

    @Override
    public void dispatchingQuestion(int tick, Question q, Player p) {
        publisher.publish(new QuestionDispatchedEvent(tick, q, p.username()));

    }

    @Override
    public void playerLost(int tick, String username, double amount, String reason) {
        publisher.publish(new PlayerCashLostEvent(tick, username, amount, reason));
    }

    @Override
    public void playerWon(int tick, String username, double amount) {
        publisher.publish(new PlayerCashWonEvent(tick, username, amount));
    }

    @Override
    public void playerOnline(int tick, String username, boolean online) {
        publisher.publish(new PlayerOnlineStatusChangedEvent(tick, username, online));
    }
}
