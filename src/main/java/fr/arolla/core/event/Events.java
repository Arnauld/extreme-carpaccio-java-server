package fr.arolla.core.event;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by fmaury on 13/11/16.
 */
public interface Events {

    /**
     * Permet de recuperer tout les evenements
     * @return les événements
     */
    List<IdentifiableEvent> all();

    /**
     * cherche des evenements spécifiques
     * @return les evenements repondants aux criteres
     */
    List<IdentifiableEvent> search(EventQuery query);

    /**
     * Persiste un evenement
     * @param event l'evenement à persister
     */
    void save(IdentifiableEvent event);

    /**
     * Requête pour filtrer les evenements.
     * Fournit des predicats
     */
    class EventQuery {
        private Optional<Integer> fromTick;
        private Optional<String> username;

        public EventQuery(Integer fromTick, String username) {
            this.fromTick = Optional.ofNullable(fromTick);
            this.username = Optional.ofNullable(username);
        }

        public EventQuery(Optional<Integer> fromTick, String username) {
            this.fromTick = fromTick;
            this.username = Optional.ofNullable(username);
        }

        public Predicate<IdentifiableEvent> getTickPredicate() {
            return fromTick.map(startTick->(Predicate<IdentifiableEvent>)(e->e.getTick()>startTick)).orElse(t->true);
        }

        public Predicate<IdentifiableEvent> getUsernamePredicate() {
            return username.map(expectedUsername -> (Predicate<IdentifiableEvent>) (e -> e.getUsername().equals(expectedUsername))).orElse(t -> true);
        }

    }

}
