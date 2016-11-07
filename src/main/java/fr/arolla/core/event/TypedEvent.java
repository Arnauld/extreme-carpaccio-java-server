package fr.arolla.core.event;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class TypedEvent {
    public final String type;

    public TypedEvent() {
        this.type = getClass().getSimpleName();
    }
}
