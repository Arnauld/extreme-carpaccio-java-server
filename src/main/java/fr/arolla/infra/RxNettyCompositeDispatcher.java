package fr.arolla.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.Feedback;
import fr.arolla.core.FeedbackSender;
import fr.arolla.core.Player;
import fr.arolla.core.Question;
import fr.arolla.core.QuestionDispatcher;
import fr.arolla.core.QuestionOfPlayer;
import fr.arolla.core.question.CorruptedQuestion;
import fr.arolla.util.Randomizator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class RxNettyCompositeDispatcher implements QuestionDispatcher, FeedbackSender {
    private final RxNettyDispatcher dispatcher;
    private final RxNettyCorruptedDispatcher corruptedDispatcher;

    @Autowired
    public RxNettyCompositeDispatcher(ObjectMapper objectMapper, Randomizator randomizator) {
        this.dispatcher = new RxNettyDispatcher(objectMapper);
        this.corruptedDispatcher = new RxNettyCorruptedDispatcher(randomizator);
    }

    @Override
    public void notify(int tick, Feedback feedback) {
        dispatcher.notify(tick, feedback);
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question q, Player p) {
        if (q instanceof CorruptedQuestion)
            return corruptedDispatcher.dispatchQuestion(tick, q, p);
        return dispatcher.dispatchQuestion(tick, q, p);
    }
}
