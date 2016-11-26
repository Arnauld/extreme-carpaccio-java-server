package fr.arolla.core;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by Domo-kun on 15/11/2016.
 */

public class Feedback {
    public static final Feedback NO_FEEDBACK = new Feedback(null, null, null);
    private final Player player;
    private final String message;
    private final Type type;

    private Feedback(Player player, String message, Type type) {
        this.player = player;
        this.message = message;
        this.type = type;
    }

    public static Feedback winning(QuestionOfPlayer qop) {
        String message = "Congrats " + qop.username() +
                ", your answer (" + qop.getPlayerResponse() + ") was right !" +
                "-> You just earned " + qop.gainAmount();
        return new Feedback(qop.getPlayer()
                , message
                , Type.WIN);
    }

    public static Feedback losing(QuestionOfPlayer qop) {
        String message = "OMG " + qop.username() +
                ", we expected (" + qop.getExpectedResponse() + ")" +
                " but your answer was (" + qop.getPlayerResponse() + ")" +
                "-> You will be charged of " + qop.gainPenalty();
        return new Feedback(qop.getPlayer(), message, Type.LOOSE);
    }

    public static Feedback error(QuestionOfPlayer qop, double amount) {
        String message = "Holy crap " + qop.username() +
                ", an error happened :(" +
                "-> You will be charged of " + -amount;
        return new Feedback(qop.getPlayer(), message, Type.ERROR);
    }

    public boolean hasFeedback() {
        return !this.equals(NO_FEEDBACK);
    }

    public Player getPlayer() {
        return player;
    }

    public Object data() {
        return new Data(message, type);
    }

    public enum Type {
        ERROR, WIN, LOOSE
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        public final String message;
        public final Type type;

        public Data(String message, Type type) {
            this.message = message;
            this.type = type;
        }
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "player=" + player.username() +
                ", message='" + message + '\'' +
                ", type=" + type +
                '}';
    }
}
