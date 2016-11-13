package fr.arolla.core.event;


public class IdentifiableEvent extends TypedEvent{
    private final String username;
    private final int tick;

    public IdentifiableEvent(String username, int tick) {
        super();
        this.username = username;
        this.tick = tick;
    }

    public String getUsername() {
        return username;
    }

    public int getTick() {
        return tick;
    }
}
