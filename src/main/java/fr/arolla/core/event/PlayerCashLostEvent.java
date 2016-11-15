package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashLostEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final double amount;
    private final String reason;
    private final String username;
    private final int tick;

    public PlayerCashLostEvent(int tick, String username, double amount, String reason) {
        super();
        this.tick=tick;
        this.amount = amount;
        this.reason = reason;
        this.username=username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTick() {
        return tick;
    }

    public double getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }
}
