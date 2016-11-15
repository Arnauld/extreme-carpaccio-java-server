package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionOfPlayer {

    public enum Status {
        OK,
        UnreachablePlayer,
        InvalidResponse,
        NoResponseReceived,
        NotSent,
        Timeout,
        QuestionRejected,
        Error
    }

    private final Question question;
    private final Player player;
    private Status status = Status.NotSent;
    private Question.Response response;

    public QuestionOfPlayer(Question question, Player player) {
        this.question = question;
        this.player = player;
    }

    public boolean isPlayerOnline() {
        return player.isOnline();
    }

    public Status status() {
        return status;
    }

    public QuestionOfPlayer withStatus(Status status) {
        this.status = status;
        return this;
    }

    public QuestionOfPlayer withResponse(Question.Response response) {
        this.response = response;
        return this;
    }

    public boolean isResponseAccepted() {
        return question.accepts(response).accepted();
    }

    public boolean isInvalidQuestion() {
        return question.isInvalid();
    }

    public String username() {
        return player.username();
    }

    public double gainAmount() {
        return question.gainAmount();
    }

    public double gainPenalty() {
        return question.lossPenalty();
    }

    public double lossErrorPenalty() {
        return question.lossErrorPenalty();
    }

    public double lossOfflinePenalty() {
        return question.lossOfflinePenalty();
    }

    public Player getPlayer() {
        return player;
    }

    public String getExpectedResponse() {
        return question.accepts(response).diagnostic();
    }

    public String getPlayerResponse() {
        return response.toString();
    }
}
