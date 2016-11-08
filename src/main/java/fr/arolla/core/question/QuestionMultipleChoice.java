package fr.arolla.core.question;

import fr.arolla.core.Question;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionMultipleChoice extends QuestionSupport implements Question {

    public static class Data {
        public final String question;
        public final List<String> choices;

        public Data(String question, List<String> choices) {
            this.question = question;
            this.choices = choices;
        }
    }

    private final Data data;
    private final Predicate<String> validator;

    public QuestionMultipleChoice(String question, List<String> choices, String response) {
        this(question, choices, r -> r != null && response.equalsIgnoreCase(r));
    }

    public QuestionMultipleChoice(String question, Predicate<String> validator) {
        this(question, null, validator);
    }

    public QuestionMultipleChoice(String question, List<String> choices, Predicate<String> validator) {
        this.data = new Data(question, choices);
        this.validator = validator;
    }

    @Override
    public Object questionData() {
        return data;
    }

    /**
     * @param total    must be null
     * @param response nust not be null
     */
    @Override
    public boolean accepts(Double total, String response) {
        return total == null && response != null && validator.test(response);
    }
}
