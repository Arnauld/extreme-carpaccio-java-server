package fr.arolla.core.event;

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
    List<CarpaccioEvent> all();

    /**
     * cherche des evenements spécifiques
     * @return les evenements repondants aux criteres
     */
    List<CarpaccioEvent> search(EventQuery query);

    /**
     * Persiste un evenement
     * @param event l'evenement à persister
     */
    void save(CarpaccioEvent event);

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

        public Predicate<CarpaccioEvent> getTickPredicate() {
            return fromTick
                    .map(hasSameTick())
                    .orElse(t -> true);
        }

        private Function<Integer, Predicate<CarpaccioEvent>> hasSameTick() {
            return startTick->(Predicate<CarpaccioEvent>)(e->e.getTick()>startTick);
        }

        public Predicate<CarpaccioEvent> getUsernamePredicate() {
            return username
                    .map(hasSameUsername())
                    .orElse(t -> true);
        }

        private Function<String, Predicate<CarpaccioEvent>> hasSameUsername() {
            return expectedUsername ->
                    hasUsername()
                    .and(hasSameUsername(expectedUsername));
        }

        private Predicate<CarpaccioEvent> hasSameUsername(String expectedUsername) {
            return e -> ((HasUsername) e).getUsername().equals(expectedUsername);
        }

        private Predicate<CarpaccioEvent> hasUsername() {
            return (Predicate<CarpaccioEvent>) (e -> e instanceof HasUsername);
        }

    }

}
