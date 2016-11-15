package fr.arolla.core.question;

import fr.arolla.core.Question;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class QuestionMultipleChoiceTest {

    @Test
    public void should_illustrate__ask_your_name() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What's your name?",
                s -> Pattern.compile("[^0-9]+").matcher(s).matches());

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(r("Arnauld")).accepted()).isTrue();
    }

    private static Question.Response r(Object content) {
        return new ResponseSupport("response", content);
    }

    private static Question.Response r() {
        return new ResponseSupport(Collections.emptyMap());
    }

    @Test
    public void should_support_edge_cases() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What's your name?",
                s -> Pattern.compile("[^0-9]+").matcher(s).matches());

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(r("Arnauld")).accepted()).isTrue();
        assertThat(q.accepts(r(null)).accepted()).isFalse();
        assertThat(q.accepts(r(12.4)).accepted()).isFalse();
        assertThat(q.accepts(r()).accepted()).isFalse();
    }

    @Test
    public void should_illustrate__multiple_choice__basic_usecase() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What is the best programming Language?",
                asList("java", "c#", "javascript", "lisp", "erlang", "cobol", "clojure", "f#"),
                "ERLANG");

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(r("ErlAng")).accepted()).describedAs("accept response ignoring case").isTrue();
        assertThat(q.accepts(r("c#")).accepted()).isFalse();
        assertThat(q.accepts(r("cobol")).accepted()).isFalse();
    }

    @Test
    public void should_illustrate__multiple_choice__any_response() {
        List<String> languages = asList("java", "c#", "javascript", "lisp", "erlang", "cobol", "clojure", "f#");

        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What is the best programming Language?",
                languages,
                r -> languages.stream().anyMatch(p -> p.equalsIgnoreCase(r)));

        assertThat(q.isInvalid()).isFalse();
        for (String language : languages) {
            assertThat(q.accepts(r(language.toUpperCase())).accepted()).isTrue();
        }
        assertThat(q.accepts(r()).accepted()).isFalse();
        assertThat(q.accepts(r(16.8)).accepted()).isFalse();
        assertThat(q.accepts(r(null)).accepted()).isFalse();
    }

    @Test
    public void should_illustrate__multiple_choice__response_not_given() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What is the best programming Language?",
                asList("java", "c#", "javascript", "lisp", "cobol", "clojure", "f#"),
                "ERLANG");

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(r("ErlAng")).accepted()).describedAs("accept response ignoring case").isTrue();
        assertThat(q.accepts(r("c#")).accepted()).isFalse();
        assertThat(q.accepts(r("cobol")).accepted()).isFalse();
        assertThat(q.accepts(r(null)).accepted()).isFalse();
        assertThat(q.accepts(r(16.8)).accepted()).isFalse();
    }
}