package fr.arolla.core.question;

import fr.arolla.core.Question;
import fr.arolla.core.QuestionGenerator;
import fr.arolla.util.Randomizator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class QuestionGeneratorBasic implements QuestionGenerator {

    private final Randomizator randomizator;
    private final Taxes taxes;

    @Autowired
    public QuestionGeneratorBasic(Randomizator randomizator) {
        this(randomizator, Taxes.defaultTaxes());
    }

    public QuestionGeneratorBasic(Randomizator randomizator, Taxes taxes) {
        this.randomizator = randomizator;
        this.taxes = taxes;
    }

    @Override
    public Question nextQuestion(int tick) {
        return randomPriceQuestion();
    }

    private Question randomPriceQuestion() {
        int sz = randomizator.randomInt(15);
        int[] quantities = randomizator.randomPositiveInts(sz, 100);
        double[] prices = randomizator.randomPositiveDoubles(sz, 1000.0d);
        Taxes.Country country = randomizator.pickOne(taxes.countries());
        ReductionMode reductionMode = randomizator.pickOne(ReductionMode.values());
        return new QuestionPriceCalculation(
                quantities, prices, country, reductionMode,
                taxes.taxOf(country),
                reductionMode::applyReduction);
    }

}
