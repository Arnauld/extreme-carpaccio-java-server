package fr.arolla.core.event;


public class TypedEvent {
    public final String type;

    public TypedEvent() {
        this.type = getClass().getSimpleName();
    }
}
