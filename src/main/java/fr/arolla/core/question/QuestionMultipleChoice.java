package fr.arolla.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.arolla.core.Question;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionMultipleChoice extends QuestionSupport implements Question {

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    public QuestionMultipleChoice(@Nonnull String question,
                                  @Nonnull List<String> choices,
                                  @Nonnull String response) {
        this(question, choices, r -> r != null && response.equalsIgnoreCase(r));
    }

    public QuestionMultipleChoice(@Nonnull String question,
                                  @Nonnull Predicate<String> validator) {
        this(question, null, validator);
    }

    public QuestionMultipleChoice(@Nonnull String question,
                                  @Nullable List<String> choices,
                                  @Nonnull Predicate<String> validator) {
        this.data = new Data(question, choices);
        this.validator = validator;
    }

    @Override
    public Object questionData() {
        return data;
    }

    /**
     * @param response nust not be null
     */
    @Override
    public boolean accepts(@Nonnull Response response) {
        return response
                .get("response", String.class)
                .map(validator::test)
                .orElse(false);
    }
}
