//
// Three fields need to be defined and will be retrieved from loading the script
//
// * version
// * weight
// * generator
//
import fr.arolla.core.Question
import fr.arolla.core.QuestionGenerator
import fr.arolla.core.question.CorruptedQuestion
import fr.arolla.core.question.QuestionMultipleChoice
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

weight = 0.0 as double

// ----------------------------------------------------------------------------
//
// QUESTIONS
//
// ----------------------------------------------------------------------------
Randomizator random = new Randomizator();

def questions = [
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.Random),
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.ContentLength),
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.Accept),
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.ContentType),
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.Random,CorruptedQuestion.Header.Accept),
        new CorruptedQuestion().headersToPlayWith(CorruptedQuestion.Header.ContentLength,CorruptedQuestion.Header.Accept)
] as List


public class QuestionChoiceGenerator implements QuestionGenerator {

    List<QuestionMultipleChoice> questions

    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        return randomizator.pickOne(questions)
    }
}

generator = new QuestionChoiceGenerator(questions: questions)