package fr.arolla.core.question;

import fr.arolla.core.Question;
import fr.arolla.core.QuestionGenerator;
import fr.arolla.util.Randomizator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionGeneratorComposite implements QuestionGenerator {

    private final List<WGen> wGenerators = new ArrayList<>();

    public void register(HasWeight weight, QuestionGenerator generator) {
        wGenerators.add(new WGen(weight, generator));
    }

    @Override
    public Question nextQuestion(int tick, Randomizator randomizator) {
        return randomizator.pickOne(wGenerators, g -> g.weight(tick)).nextQuestion(tick, randomizator);
    }

    private static class WGen implements HasWeight, QuestionGenerator {
        public final HasWeight weight;
        public final QuestionGenerator generator;

        private WGen(HasWeight weight, QuestionGenerator generator) {
            this.weight = weight;
            this.generator = generator;
        }

        @Override
        public double weight(int tick) {
            return weight.weight(tick);
        }

        @Override
        public Question nextQuestion(int tick, Randomizator randomizator) {
            return generator.nextQuestion(tick, randomizator);
        }
    }
}
