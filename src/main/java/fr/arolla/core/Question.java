package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Question {
    /**
     * Data that should be (serialized and) sent to the player.
     */
    Object questionData();

    /**
     * Validate the response of the question.
     */
    boolean accepts(Double total, String response);

    /**
     * Indicates whether or not the response of the question should be invalid.
     * This can be the case if the question itself is intended to be corrupted.
     */
    boolean isInvalid();

    /**
     * Amount earned if the answer is accepted.
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Double, String)
     */
    double gainAmount();

    /**
     * Amount lost if the answer is not accepted
     * It is up to the question to determinate this amount, based on the question difficulty for example.
     *
     * @see #accepts(Double, String)
     */
    double lossPenalty();
}
