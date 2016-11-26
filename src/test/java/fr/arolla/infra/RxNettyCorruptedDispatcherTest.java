package fr.arolla.infra;


import fr.arolla.core.Player;
import fr.arolla.core.question.CorruptedQuestion;
import fr.arolla.util.Randomizator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class RxNettyCorruptedDispatcherTest {

    private Randomizator randomizator;
    private RxNettyCorruptedDispatcher dispatcher;
    private Player player;

    @Before
    public void setUp() {
        randomizator = new Randomizator();
        dispatcher = new RxNettyCorruptedDispatcher(randomizator);
        player = new Player("az", "az", "http://localhost:8990");
    }

    @Test
    public void usecase() throws IOException {
        CorruptedQuestion question =
                new CorruptedQuestion()
                        .headersToPlayWith(
                                CorruptedQuestion.Header.ContentLength
                                , CorruptedQuestion.Header.ContentType
                                //        ,CorruptedQuestion.Header.Random
                        )
                        .minBytesToWrite(1_000_000L)
                        .maxBytesToWrite(8_000_000L)
                //        .bytes(Arrays.asList("Hello".getBytes()))
                ;
        dispatcher
                .dispatchQuestion(1, question, player)
                .toBlocking()
                .first();
    }
}