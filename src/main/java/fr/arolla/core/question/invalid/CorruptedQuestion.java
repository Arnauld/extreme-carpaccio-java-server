package fr.arolla.core.question.invalid;

/**
 * Created by fmaury on 17/11/16.
 */
public class CorruptedQuestion extends InvalidQuestionSupport{


    @Override
    public Object questionData() {
        return "{need to send end of file}";
    }


    @Override
    public boolean isCorrupted(){
        return true;
    }
}
