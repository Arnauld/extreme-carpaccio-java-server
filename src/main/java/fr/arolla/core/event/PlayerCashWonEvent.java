package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashWonEvent extends TypedEvent implements Event,HasUsername,HasTick {
    private final double amount;
    private final String username;
    private final int tick;

    public PlayerCashWonEvent(int tick, String username, double amount) {
        super();
        this.tick=tick;
        this.amount = amount;
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
}
