package script

import fr.arolla.core.question.QuestionMultipleChoice

import java.util.function.Predicate
import java.util.stream.Collectors

def q(String question, Predicate<String> validator) {

questions = [
        q("What's the name of your team?", s -> true )
]