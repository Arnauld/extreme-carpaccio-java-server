package fr.arolla.core.event;


import fr.arolla.core.Event;

public class TypedEvent implements Event {

    private final String type;

    public TypedEvent() {
        this.type = getClass().getSimpleName();
    }

    public String getType() {
        return type;
    }
}
