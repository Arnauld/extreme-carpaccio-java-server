package fr.arolla.core.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionPriceCalculationTest {
    private DoubleUnaryOperator taxFn = t -> t * 1.1d;
    private DoubleUnaryOperator reductionFn = t -> t * 0.75d;

    @Test
    public void should_not_be_an_invalid_question_with_default_gain_and_loss() throws JsonProcessingException {
        int[] quantities = {1, 2, 3, 4};
        double[] prices = {1.0d, 2.0d, 3.0d, 4.0d};

        QuestionPriceCalculation q =
                new QuestionPriceCalculation(
                        quantities,
                        prices,
                        Taxes.Country.FR,
                        ReductionMode.HalfPrice,
                        taxFn,
                        reductionFn);

        assertThat(q.isInvalid()).isFalse();
        assertThat(q.gainAmount()).isEqualTo(QuestionPriceCalculation.GAIN_AMOUNT);
        assertThat(q.lossPenalty()).isEqualTo(QuestionPriceCalculation.LOSS_PENALTY_VALID);
    }

    @Test
    public void should_serialize_question_data_into_json() throws JsonProcessingException {
        int[] quantities = {1, 2, 3, 4};
        double[] prices = {1.0d, 2.0d, 3.0d, 4.0d};

        QuestionPriceCalculation q =
                new QuestionPriceCalculation(
                        quantities,
                        prices,
                        Taxes.Country.FR,
                        ReductionMode.HalfPrice,
                        taxFn,
                        reductionFn);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(q.questionData());

        assertThat(json).contains("\"quantities\":[1,2,3,4]");
        assertThat(json).contains("\"prices\":[1.0,2.0,3.0,4.0");
        assertThat(json).contains("\"country\":\"FR\"");
        assertThat(json).contains("\"reductionMode\":\"HalfPrice\"");
    }

    @Test
    public void should_accept_correct_amount__basic_case() {
        int[] quantities = {1, 2, 3, 4};
        double[] prices = {1.0d, 2.0d, 3.0d, 4.0d};

        QuestionPriceCalculation q =
                new QuestionPriceCalculation(
                        quantities,
                        prices,
                        Taxes.Country.FR,
                        ReductionMode.HalfPrice,
                        taxFn,
                        reductionFn);

        double total = reductionFn.applyAsDouble(taxFn.applyAsDouble(1 * 1.0 + 2 * 2.0 + 3 * 3.0 + 4 * 4.0));
        assertThat(q.accepts(total, null)).isTrue();
    }

    @Test
    public void should_accept_correct_amount__empty_case() {
        int[] quantities = {};
        double[] prices = {};

        QuestionPriceCalculation q =
                new QuestionPriceCalculation(
                        quantities,
                        prices,
                        Taxes.Country.FR,
                        ReductionMode.HalfPrice,
                        taxFn,
                        reductionFn);

        double total = 0.0d;
        assertThat(q.accepts(total, null)).isTrue();
    }
}