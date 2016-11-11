package fr.arolla.core;

import fr.arolla.util.Randomizator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.Subscriber;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class Game {
    private final Logger log = LoggerFactory.getLogger(Game.class);

    private final GameListener listener;
    private final Players players;
    private final QuestionGenerator questionGenerator;
    private final QuestionDispatcher dispatcher;
    private final Randomizator randomizator;
    private double offlinePenalty = -500.0d;
    private double errorPenalty = -450.0d;

    @Autowired
    public Game(GameListener listener,
                Players players,
                QuestionGenerator questionGenerator,
                QuestionDispatcher dispatcher,
                Randomizator randomizator) {
        this.listener = listener;
        this.players = players;
        this.questionGenerator = questionGenerator;
        this.dispatcher = dispatcher;
        this.randomizator = randomizator;
    }

    public Game withOfflinePenalty(double offlinePenalty) {
        this.offlinePenalty = offlinePenalty;
        return this;
    }

    public Game withErrorPenalty(double errorPenalty) {
        this.errorPenalty = errorPenalty;
        return this;
    }

    public void processIteration(int tick) {
        listener.iterationStarting(tick);

        Question q = questionGenerator.nextQuestion(tick, randomizator);
        List<Observable<QuestionOfPlayer>> dispatched =
                players.all()
                        .map(p -> dispatch(tick, q, p))
                        .collect(Collectors.toList());
        Observable.merge(dispatched)
                .subscribe(collectResponse(tick));
    }

    private Subscriber<? super QuestionOfPlayer> collectResponse(int tick) {
        return new Subscriber<QuestionOfPlayer>() {
            @Override
            public void onCompleted() {
                log.info("All responses received for tick {}.", tick);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Ooops (during tick {})", tick, throwable);
            }

            @Override
            public void onNext(QuestionOfPlayer questionOfPlayer) {
                evaluateAnswer(questionOfPlayer, tick);
            }
        };
    }

    private void evaluateAnswer(QuestionOfPlayer qop, int tick) {
        boolean online = true;
        switch (qop.status()) {
            case OK:
                if (qop.isResponseAccepted()) {
                    players.addCash(qop.username(), qop.gainAmount());
                    listener.playerWon(tick, qop.username(), qop.gainAmount());
                } else {
                    players.addCash(qop.username(), qop.gainPenalty());
                    listener.playerLost(tick, qop.username(), qop.gainPenalty(), "response-invalid");
                }
                break;
            case QuestionRejected:
                if (qop.isInvalidQuestion()) {
                    players.addCash(qop.username(), qop.gainAmount());
                    listener.playerWon(tick, qop.username(), qop.gainAmount());
                } else {
                    players.addCash(qop.username(), qop.gainPenalty());
                    listener.playerLost(tick, qop.username(), qop.gainPenalty(), "response-invalid");
                }
                break;
            case UnreachablePlayer:
            case Timeout:
            case NoResponseReceived:
                online = false;
                players.addCash(qop.username(), lossOfflinePenalty());
                listener.playerLost(tick, qop.username(), lossOfflinePenalty(), "timeout");
                break;
            case InvalidResponse:
                players.addCash(qop.username(), lossOfflinePenalty());
                listener.playerLost(tick, qop.username(), lossOfflinePenalty(), "timeout");
                break;
            case Error:
                online = false;
                players.addCash(qop.username(), lossErrorPenalty());
                listener.playerLost(tick, qop.username(), lossErrorPenalty(), "error");
                break;
            case NotSent:
            default:
                online = false;
                log.warn("Unsupported status {} for player {} (during tick {})", qop.status(), qop.username(), tick);
        }

        markPlayerOnline(tick, qop, online);
    }

    private void markPlayerOnline(int tick, QuestionOfPlayer qop, boolean online) {
        if (online == qop.isPlayerOnline())
            return;
        players.markPlayerOnline(qop.username(), online);
        listener.playerOnline(tick, qop.username(), online);
    }

    private double lossErrorPenalty() {
        return errorPenalty;
    }

    private double lossOfflinePenalty() {
        return offlinePenalty;
    }

    private Observable<QuestionOfPlayer> dispatch(int tick, Question q, Player p) {
        listener.dispatchingQuestion(tick, q, p);
        return dispatcher.dispatchQuestion(tick, q, p);
    }
}
