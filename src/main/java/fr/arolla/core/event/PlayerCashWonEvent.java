package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashWonEvent extends IdentifiableEvent implements Event {
    public final double amount;

    public PlayerCashWonEvent(int tick, String username, double amount) {
        super(username, tick);
        this.amount = amount;
    }
}
