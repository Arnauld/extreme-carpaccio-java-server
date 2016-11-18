package fr.arolla.core.question.invalid;

/**
 * Created by fmaury on 17/11/16.
 */
public class EOFQuestion extends InvalidQuestionSupport{


    @Override
    public Object questionData() {
        return "{nom:toto;prenom:boby;nom:toto;prenom:boby;nom:toto;prenom:boby;nom:toto;prenom:boby;nom:toto;prenom:boby;nom:toto;prenom:boby;nom:toto;prenom:boby}";
    }
}
