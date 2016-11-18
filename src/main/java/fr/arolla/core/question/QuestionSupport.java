package fr.arolla.core.question;

import fr.arolla.core.Question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public abstract class QuestionSupport implements Question {


    public static final double GAIN_AMOUNT = 450;
    public static final double LOSS_PENALTY_INVALID = -100;
    public static final double LOSS_PENALTY_VALID = -250;
    public static final double LOSS_PENALTY_OFFLINE = -500.0d;
    public static final double LOSS_PENALTY_ERROR = -450.0d;

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

    @Override
    public double lossOfflinePenalty() {
        return LOSS_PENALTY_OFFLINE;
    }

    @Override
    public double lossErrorPenalty() {
        return LOSS_PENALTY_ERROR;
    }

    @Override
    public boolean isCorrupted(){
        return false;
    }
}
