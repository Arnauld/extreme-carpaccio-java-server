package fr.arolla.core.event;

import fr.arolla.core.Event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PlayerCashLostEvent extends CarpaccioEvent implements Event,HasUsername {
    public final double amount;
    public final String reason;
    private final String username;

    public PlayerCashLostEvent(int tick, String username, double amount, String reason) {
        super(tick);
        this.amount = amount;
        this.reason = reason;
        this.username=username;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
