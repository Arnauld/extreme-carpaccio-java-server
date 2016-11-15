package fr.arolla.core;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Question {

    interface Response {
        <T> Optional<T> get(String key, Class<T> type);
    }

    interface ResponseValidation {
        boolean accepted();

        String diagnostic();
    }

    /**
     * Data that should be (serialized and) sent to the player.
     */
    Object questionData();

    /**
     * Validate the response of the question.
     */
    boolean accepts(@NotNull Response response);

    /**
     * Indicates whether or not the response of the question should be invalid.
     * This can be the case if the question itself is intended to be corrupted.
     */
    boolean isInvalid();

    /**
     * Amount earned if the answer is accepted.
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Response)
     */
    double gainAmount();

    /**
     * Amount lost if the answer is not accepted
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Response)
     */
    double lossPenalty();

    String expectedResponse();


}
