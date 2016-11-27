package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface GameListener {
    void iterationStarting(int tick);

    void dispatchingQuestion(int tick, Question q, Player p);

    void playerLost(int tick, String username, double amount, String reason);

    void playerWon(int tick, String username, double amount);

    void playerOnline(int tick, String username, boolean online);

    void playerSkipQuestion(int tick, String username);
}
