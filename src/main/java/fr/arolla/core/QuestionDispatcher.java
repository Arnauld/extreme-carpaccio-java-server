package fr.arolla.core;

import rx.Observable;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface QuestionDispatcher {
    Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question q, Player p);
}
