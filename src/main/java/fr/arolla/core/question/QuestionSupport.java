package fr.arolla.core.question;

import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public abstract class QuestionSupport implements Question {


    public static final int GAIN_AMOUNT = 450;
    public static final int LOSS_PENALTY_INVALID = -100;
    public static final int LOSS_PENALTY_VALID = -250;

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Override
    public double gainAmount() {
        return GAIN_AMOUNT;
    }

    @Override
    public double lossPenalty() {
        return isInvalid() ? LOSS_PENALTY_INVALID : LOSS_PENALTY_VALID;
    }
}
