package fr.arolla.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service("CompositeDispatcher")
public class CompositeDispatcher implements QuestionDispatcher, FeedbackSender {

    private final Logger log = LoggerFactory.getLogger(CompositeDispatcher.class);

    private final RxNettyDispatcher defaultDispatcher;
    private final QuestionDispatcher dosDispatcher;

    @Autowired
    public CompositeDispatcher(ObjectMapper objectMapper) {
        this.defaultDispatcher=new RxNettyDispatcher(objectMapper);
        this.dosDispatcher=new RxNettyCorruptionDispatcher(objectMapper);
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question question, Player player) {
            if(question.isCorrupted()){
               return dosDispatcher.dispatchQuestion(tick, question, player);
            }
            return defaultDispatcher.dispatchQuestion(tick, question, player);
    }

    @Override
    public void notify(Feedback feedback, int tick) {
        defaultDispatcher.notify(feedback,tick);
    }

}
