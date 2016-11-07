package fr.arolla.core;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import rx.Observable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class GameTest {

    private Game game;
    private GameListener listener;
    private Players players;
    private QuestionGenerator questionGenerator;
    private QuestionDispatcher dispatcher;
    private Player p1, p2;
    //
    private QuestionOfPlayer.Status[] qopStatus;
    private String[] responseValues;

    @Before
    public void setUp() {
        p1 = new Player("travis", "tr", "http://travis.pac.man");
        p2 = new Player("carmen", "cr", "http://travis.pac.man");

        listener = mock(GameListener.class);
        players = mock(Players.class);
        questionGenerator = mock(QuestionGenerator.class);
        dispatcher = mock(QuestionDispatcher.class);

        game = new Game(listener, players, questionGenerator, dispatcher);
    }

    @Test
    public void should_dispatch_question_to_all_registered_players() {
        //
        // GIVEN
        //
        int tick = 17;
        QuestionString question = new QuestionString("Vlad");
        AtomicInteger invocationCount = new AtomicInteger();

        // suppose everyone answered with an OK response...
        qopStatus = array(QuestionOfPlayer.Status.OK, QuestionOfPlayer.Status.OK);
        // ... but with different responses
        responseValues = array("Vlad", "Duke");

        when(questionGenerator.nextQuestion(anyInt())).thenReturn(question);
        when(players.all()).thenReturn(Arrays.asList(p1, p2).stream());
        when(dispatcher.dispatchQuestion(anyInt(), any(Question.class), any(Player.class)))
                .then(invocation -> fakeDispatch(invocationCount.getAndIncrement(), invocation));

        //
        // WHEN
        //
        game.processIteration(tick);

        //
        // THEN
        //

        // question has been dispatched to both players
        verify(dispatcher).dispatchQuestion(tick, question, p1);
        verify(dispatcher).dispatchQuestion(tick, question, p2);
    }


    @Test
    public void should_adjust_gain_based_on_response_validity() {
        //
        // GIVEN
        //
        int tick = 17;
        QuestionString question = new QuestionString("Vlad");
        AtomicInteger invocationCount = new AtomicInteger();

        // suppose everyone answered with an OK response...
        qopStatus = array(QuestionOfPlayer.Status.OK, QuestionOfPlayer.Status.OK);
        // ... but with different responses
        responseValues = array("Vlad", "Duke");

        when(questionGenerator.nextQuestion(anyInt())).thenReturn(question);
        when(players.all()).thenReturn(Arrays.asList(p1, p2).stream());
        when(dispatcher.dispatchQuestion(anyInt(), any(Question.class), any(Player.class)))
                .then(invocation -> fakeDispatch(invocationCount.getAndIncrement(), invocation));

        //
        // WHEN
        //
        game.processIteration(tick);

        //
        // THEN
        //
        ArgumentCaptor<Double> amount1 = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> amount2 = ArgumentCaptor.forClass(Double.class);

        // Player 1 answered the right response 'Vlad' according to the QuestionString
        verify(players).addCash(eq(p1.username()), amount1.capture());
        assertThat(amount1.getAllValues()).containsExactly(QuestionString.GAIN_AMOUNT);

        // Player 2 answered a wrong response 'Duke'
        verify(players).addCash(eq(p2.username()), amount2.capture());
        assertThat(amount2.getAllValues()).containsExactly(QuestionString.GAIN_PENALTY);
    }

    private Observable<QuestionOfPlayer> fakeDispatch(int invocationCount, InvocationOnMock invocation) {
        Object[] arguments = invocation.getArguments();
        assertThat(arguments[0]).isInstanceOf(Integer.class);
        assertThat(arguments[1]).isInstanceOf(Question.class);
        assertThat(arguments[2]).isInstanceOf(Player.class);

        QuestionOfPlayer qop =
                new QuestionOfPlayer((Question) arguments[1], (Player) arguments[2])
                        .withStatus(qopStatus[invocationCount])
                        .withResponse(0.0d, responseValues[invocationCount]);
        return Observable.just(qop);
    }

    private static <T> T[] array(T... xs) {
        return xs;
    }

}