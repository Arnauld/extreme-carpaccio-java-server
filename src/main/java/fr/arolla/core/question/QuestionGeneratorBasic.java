package fr.arolla.core.question;

import fr.arolla.core.Question;
import fr.arolla.core.QuestionGenerator;
import fr.arolla.util.Randomizator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionGeneratorBasic implements QuestionGenerator {

    private final Taxes taxes;

    @Autowired
    public QuestionGeneratorBasic() {
        this(Taxes.defaultTaxes());
    }

    public QuestionGeneratorBasic(Taxes taxes) {
        this.taxes = taxes;
    }

    @Override
    public Question nextQuestion(int tick, Randomizator randomizator) {
        return randomPriceQuestion(randomizator);
    }

    private Question randomPriceQuestion(Randomizator randomizator) {
        int sz = randomizator.randomInt(5);
        int[] quantities = randomizator.randomPositiveInts(sz, 10);
        double[] prices = randomizator.randomPositiveDoubles(sz, 100.0d);
        Country country = randomizator.pickOne(taxes.countries(), Country::populationInMillions);
        ReductionMode reductionMode = randomizator.pickOne(ReductionMode.values());
        return new QuestionPriceCalculation(
                quantities, prices, country, reductionMode,
                taxes.taxOf(country),
                reductionMode::applyReduction);
    }

}
