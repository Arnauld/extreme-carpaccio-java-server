package fr.arolla.core.question;

import fr.arolla.core.Question;

import java.util.function.Function;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionPriceCalculation extends QuestionSupport implements Question {

    private static final double EPS = 1e-3;

    public static class Data {
        public final int[] quantities;
        public final double[] prices;
        public final Taxes.Country country;
        public final ReductionMode reductionMode;

        public Data(int[] quantities, double[] prices, Taxes.Country country, ReductionMode reductionMode) {
            this.quantities = quantities;
            this.prices = prices;
            this.country = country;
            this.reductionMode = reductionMode;
        }
    }

    private final Data data;
    private final Function<Double, Double> taxFn;
    private final Function<Double, Double> reductionFn;

    public QuestionPriceCalculation(int[] quantities,
                                    double[] prices,
                                    Taxes.Country country,
                                    ReductionMode reductionMode,
                                    Function<Double, Double> taxFn,
                                    Function<Double, Double> reductionFn) {
        this.data = new Data(quantities, prices, country, reductionMode);
        this.taxFn = taxFn;
        this.reductionFn = reductionFn;
    }

    @Override
    public Object questionData() {
        return data;
    }

    @Override
    public boolean accepts(Double total, String response) {
        return total != null && isTotalCorrect(total);
    }

    private boolean isTotalCorrect(double actualTotal) {
        double total = 0.0d;
        for (int i = 0, n = data.quantities.length; i < n; i++) {
            total += data.quantities[i] * data.prices[i];
        }
        total = taxFn.apply(total);
        total = reductionFn.apply(total);
        return Math.abs(actualTotal - total) < EPS;
    }

}
