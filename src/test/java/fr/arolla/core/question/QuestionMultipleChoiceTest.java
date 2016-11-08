package fr.arolla.core.question;

import org.junit.Test;

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
        assertThat(q.accepts(null, "Arnauld")).isTrue();
    }

    @Test
    public void should_support_edge_cases() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What's your name?",
                s -> Pattern.compile("[^0-9]+").matcher(s).matches());

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(null, "Arnauld")).isTrue();
        assertThat(q.accepts(null, null)).isFalse();
        assertThat(q.accepts(16.8, null)).isFalse();
    }

    @Test
    public void should_illustrate__multiple_choice__basic_usecase() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What is the best programming Language?",
                asList("java", "c#", "javascript", "lisp", "erlang", "cobol", "clojure", "f#"),
                "ERLANG");

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(null, "ErlAng")).describedAs("accept response ignoring case").isTrue();
        assertThat(q.accepts(null, "c#")).isFalse();
        assertThat(q.accepts(null, "cobol")).isFalse();
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
            assertThat(q.accepts(null, language.toUpperCase())).isTrue();
        }
        assertThat(q.accepts(null, null)).isFalse();
        assertThat(q.accepts(16.8, null)).isFalse();
    }

    @Test
    public void should_illustrate__multiple_choice__response_not_given() {
        QuestionMultipleChoice q = new QuestionMultipleChoice(
                "What is the best programming Language?",
                asList("java", "c#", "javascript", "lisp", "cobol", "clojure", "f#"),
                "ERLANG");

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.accepts(null, "ErlAng")).describedAs("accept response ignoring case").isTrue();
        assertThat(q.accepts(null, "c#")).isFalse();
        assertThat(q.accepts(null, "cobol")).isFalse();
        assertThat(q.accepts(null, null)).isFalse();
        assertThat(q.accepts(16.8, null)).isFalse();
    }
}