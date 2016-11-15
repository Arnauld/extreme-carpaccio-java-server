package fr.arolla.core.event;

import fr.arolla.core.Event;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by fmaury on 13/11/16.
 */
public interface Events {

    /**
     * Permet de recuperer tout les evenements
     * @return les événements
     */
    List<Event> all();

    /**
     * cherche des evenements spécifiques
     * @return les evenements repondants aux criteres
     */
    List<Event> search(Query query);

    /**
     * Persiste un evenement
     * @param event l'evenement à persister
     */
    void save(Event event);

    /**
     * Requête pour filtrer les evenements.
     * Fournit des predicats
     */
    class Query {
        private Optional<Integer> fromTick;
        private Optional<String> username;

        public Query(Integer fromTick, String username) {
            this.fromTick = Optional.ofNullable(fromTick);
            this.username = Optional.ofNullable(username);
        }

        public Query(Integer fromTick) {
            this.fromTick = Optional.ofNullable(fromTick);
            this.username = Optional.empty();
        }

        public Query(Optional<Integer> fromTick, String username) {
            this.fromTick = fromTick;
            this.username = Optional.ofNullable(username);
        }

        public Predicate<Event> getTickPredicate() {
            return fromTick
                    .map(hasGreaterTick())
                    .orElse(t -> true);
        }

        private Function<Integer, Predicate<Event>> hasGreaterTick() {
            return startTick->(
                    (Predicate<Event>)(e->e instanceof HasTick))
                    .and(e->((HasTick)e).getTick()>=startTick);
        }

        public Predicate<Event> getUsernamePredicate() {
            return username
                    .map(hasSameUserName())
                    .orElse(t -> true);
        }

        private Function<String, Predicate<Event>> hasSameUserName() {
            return userName->((Predicate<Event>)(e->e instanceof HasUsername)).and(e->((HasUsername)e).getUsername().equals(userName));
        }
    }

}
