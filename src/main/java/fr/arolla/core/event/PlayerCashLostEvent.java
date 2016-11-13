package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashLostEvent extends IdentifiableEvent implements Event {
    public final double amount;
    public final String reason;

    public PlayerCashLostEvent(int tick, String username, double amount, String reason) {
        super(username, tick);
        this.amount = amount;
        this.reason = reason;
    }
}
