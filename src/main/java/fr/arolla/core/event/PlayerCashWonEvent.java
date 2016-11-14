package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashWonEvent extends CarpaccioEvent implements Event,HasUsername {
    public final double amount;
    private final String username;

    public PlayerCashWonEvent(int tick, String username, double amount) {
        super(tick);
        this.amount = amount;
        this.username=username;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
