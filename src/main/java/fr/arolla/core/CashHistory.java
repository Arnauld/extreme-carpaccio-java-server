package fr.arolla.core;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class CashHistory {
    private final Player player;
    private final double[] cashHistory;

    public CashHistory(Player player, double[] cashHistory) {
        this.player = player;
        this.cashHistory = cashHistory;
    }

    public String username() {
        return player.username();
    }

    public double[] getCashHistory() {
        return cashHistory;
    }
}
