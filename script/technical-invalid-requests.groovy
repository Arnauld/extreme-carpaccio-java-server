//
// Three fields need to be defined and will be retrieved from loading the script
//
// * version
// * weight
// * generator
//
import fr.arolla.core.Question
import fr.arolla.core.QuestionGenerator
import fr.arolla.core.question.invalid.CorruptedQuestion
import fr.arolla.core.question.invalid.EOFQuestion
import fr.arolla.core.question.invalid.RandomBytesQuestion
import fr.arolla.core.question.invalid.LoremIpsumQuestion
import fr.arolla.util.Randomizator
// ----------------------------------------------------------------------------
//
// HELPER FUNCTIONS
//
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------
//
// VERSION
//
// ----------------------------------------------------------------------------

// hint field to check in log if your configuration have been reloaded

version = "1.0.0"

// ----------------------------------------------------------------------------
//
// WEIGHT
//
// ----------------------------------------------------------------------------

weight = 0.5 as double

// ----------------------------------------------------------------------------
//
// QUESTIONS
//
// ----------------------------------------------------------------------------

def questions = [
        new RandomBytesQuestion(),
        new EOFQuestion(),
        new LoremIpsumQuestion(),
        new CorruptedQuestion()
] as List


public class QuestionChoiceGenerator implements QuestionGenerator {

    List<Question> questions

    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        return randomizator.pickOne(questions)
    }
}

generator = new QuestionChoiceGenerator(questions: questions)