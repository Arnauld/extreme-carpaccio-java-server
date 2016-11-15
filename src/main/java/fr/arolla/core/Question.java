package fr.arolla.core;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.Supplier;

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

        static ResponseValidation of(boolean accepted, Supplier<String> errorMessage) {
            return new ResponseValidation() {
                @Override
                public boolean accepted() {
                    return accepted;
                }

                @Override
                public String diagnostic() {
                    return errorMessage.get();
                }
            };
        }

        static ResponseValidation rejected(String errorMessage) {
            return of(false, () -> errorMessage);
        }
    }

    /**
     * Data that should be (serialized and) sent to the player.
     */
    Object questionData();

    /**
     * Validate the response of the question.
     */
    ResponseValidation accepts(@NotNull Response response);

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
     * Amount lost if the answer is not accepted.
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Response)
     */
    double lossPenalty();

    /**
     * Amount lost if an error occured while processing this question.
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Response)
     */
    double lossErrorPenalty();

    /**
     * Amount lost if the player has been considered offline during the processing of this question.
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Response)
     */
    double lossOfflinePenalty();

}
