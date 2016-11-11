package fr.arolla.core;

import fr.arolla.util.Randomizator;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface QuestionGenerator {
    Question nextQuestion(int tick, Randomizator randomizator);
}
