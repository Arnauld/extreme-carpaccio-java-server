package fr.arolla.core.event;

import fr.arolla.core.Event;
import org.junit.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fmaury on 15/11/16.
 */
public class QueryTest {

    @Test
    public void should_return_predicate_that_not_match_with_non_tickable_Event() throws Exception {
        Event nonTickEvent = dummyEvent();
        assertThat(tickPredicateFor(0).test(nonTickEvent)).isFalse();
    }


    @Test
    public void should_return_predicate_that_match_with_HasTick_Event_with_greater_tick() throws Exception {
        Event tickedEvent = new IterationStartedEvent(12);
        assertThat(tickPredicateFor(5).test(tickedEvent)).isTrue();
    }

    @Test
    public void should_return_predicate_that_not_match_with_HasTick_Event_with_lower_tick() throws Exception {
        Event tickedEvent = new IterationStartedEvent(12);
        assertThat(tickPredicateFor(15).test(tickedEvent)).isFalse();
    }

    @Test
    public void should_return_predicate_that_match_with_HasTick_Event_with_same_tick() throws Exception {
        Event tickedEvent = new IterationStartedEvent(15);
        assertThat(tickPredicateFor(15).test(tickedEvent)).isTrue();
    }

    private Predicate<Event> tickPredicateFor(int fromTick) {
        return new Events.Query(fromTick).getTickPredicate();
    }

    private Event dummyEvent() {
        return new Event() {
        };
    }

    @Test
    public void should_return_predicate_that_not_match_with_non_nameable_Event() throws Exception {
        Event nonNameEvent = new IterationStartedEvent(12);
        assertThat(usernamePredicateFor("toto").test(nonNameEvent)).isFalse();
    }


    @Test
    public void should_return_predicate_that_match_when_username_match() throws Exception {
        Event namedEvent = new PlayerCashLostEvent(12,"toto",123,"because");
        assertThat(usernamePredicateFor("toto").test(namedEvent)).isTrue();
    }

    @Test
    public void should_return_predicate_that_not_match_when_username_is_different() throws Exception {
        Event namedEvent = new PlayerCashLostEvent(12,"toti",123,"because");
        assertThat(usernamePredicateFor("toto").test(namedEvent)).isFalse();
    }


    private Predicate<Event> usernamePredicateFor(String username) {
        return new Events.Query(0,username).getUsernamePredicate();
    }

}