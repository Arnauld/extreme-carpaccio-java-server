package fr.arolla.core.event;


import fr.arolla.core.Event;

public class CarpaccioEvent implements Event {

    public final String type;
    public final int tick;

    public CarpaccioEvent(int tick) {
        this.tick = tick;
        this.type = getClass().getSimpleName();
    }

    public String getType() {
        return type;
    }

    public int getTick() {
        return tick;
    }
}
