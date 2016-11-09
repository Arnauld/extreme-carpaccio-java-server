import fr.arolla.core.question.QuestionMultipleChoice
import fr.arolla.core.question.TaxDef

import java.util.function.Predicate
import java.util.stream.Collectors

// ----------------------------------------------------------------------------
//
// HELPER FUNCTIONS
//
// ----------------------------------------------------------------------------

def q(String question, Predicate<String> validator) {
    return new QuestionMultipleChoice(question, validator)
}

def q(String question, List<String> choices, String response) {
    return new QuestionMultipleChoice(question, choices, response)
}

def q(String question, List<String> choices, Predicate<String> validator) {
    return new QuestionMultipleChoice(question, choices, validator)
}

def tax(String country, fn) {
    return new TaxDef(country, { x -> fn(x) as double })
}

// ----------------------------------------------------------------------------
//
// QUESTIONS
//
// ----------------------------------------------------------------------------

questions = [
        q("What's the name of your team?", { s -> true }),
        q("Which word in the list designate a programming language?", ["jawa", "jaba", "java"], "java"),
        q("Is this expression valid: '2+4i'?", ['yes', 'no'], "yes"),
        q("A monoid is an algebraic structure having? (select all that applies, separated by a comma)",
                ["an identity element", "a binary associative operation", "a milkshake", "the sinus operator", "a ternary bijective operator"],
                { s ->
                    def xs = (s.split(",") as List).stream().map({ x -> x.trim() }).collect(Collectors.toList())
                    xs.containsAll("an identity element", "a binary associative operation")
                })
]

// ----------------------------------------------------------------------------
//
// TAXES
//
// ----------------------------------------------------------------------------

taxes = [
        tax("FR", { d -> d * 1.5 }),
        tax("UK", { d -> if (d > 100) d * 1.8 else d * 1.9 }),
        tax("ES", { d -> if (d > 100) d * 1.8 else d * 1.9 })
]