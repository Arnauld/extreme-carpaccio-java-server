package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashLostEvent extends TypedEvent implements Event {
    public final int tick;
    public final String username;
    public final double amount;
    public final String reason;

    public PlayerCashLostEvent(int tick, String username, double amount, String reason) {
        this.tick = tick;
        this.username = username;
        this.amount = amount;
        this.reason = reason;
    }
}
