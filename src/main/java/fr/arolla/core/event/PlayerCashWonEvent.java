package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashWonEvent extends TypedEvent implements Event {
    public final int tick;
    public final String username;
    public final double amount;

    public PlayerCashWonEvent(int tick, String username, double amount) {
        this.tick = tick;
        this.username = username;
        this.amount = amount;
    }
}
