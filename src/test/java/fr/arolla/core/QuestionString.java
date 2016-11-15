package fr.arolla.core;

import java.util.Optional;

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
    public ResponseValidation accepts(Response response) {
        Optional<String> valueOpt = response.get("response", String.class);
        if (valueOpt.isPresent())
            return ResponseValidation.of(valueOpt
                    .map(r -> r.equals(value))
                    .orElse(false), () -> "Expected: " + value + " but got: " + valueOpt.get());
        return ResponseValidation.rejected("Missing property 'response' of type String");
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
