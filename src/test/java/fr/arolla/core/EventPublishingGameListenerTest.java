package fr.arolla.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventPublishingGameListenerTest {

    private EventPublishingGameListener listener;
    private Event.Publisher publisher;

    @Before
    public void setUp() {
        publisher = mock(Event.Publisher.class);
        listener = new EventPublishingGameListener(publisher);
    }

    @Test
    public void should_dispatch_an_event_whatever_it_it() {
        Player player = new Player("az", "ar", "http://localhost:8090/zog");
        listener.dispatchingQuestion(1, new QuestionString("Yop"), player);

        verify(publisher).publish(Mockito.any(Event.class));
    }

}