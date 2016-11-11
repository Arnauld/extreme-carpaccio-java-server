package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionString implements Question {
    public static final double GAIN_AMOUNT = 450;
    public static final double GAIN_PENALTY = -250;
    private final String value;

    QuestionString(String value) {
        this.value = value;
    }

    @Override
    public Object questionData() {
        return value;
    }

    @Override
    public boolean accepts(Response response) {
        return response.get("response", String.class)
                .map(r -> r.equals(value))
                .orElse(false);
    }

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
        return GAIN_PENALTY;
    }
}
