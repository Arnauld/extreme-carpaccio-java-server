package fr.arolla.web;

import fr.arolla.command.InvalidCredentialException;
import fr.arolla.core.Event;
import fr.arolla.core.Player;
import fr.arolla.core.Players;
import fr.arolla.core.Ticker;
import fr.arolla.core.event.Events;
import fr.arolla.web.dto.PlayerRegistrationDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class WebControllerTest {

    private Players players;
    private WebController webController;
    private Ticker ticker = new Ticker();
    private Event.Publisher eventPublisher;
    private Events events;

    @Before
    public void setUp() {
        players = Mockito.mock(Players.class);
        eventPublisher = Mockito.mock(Event.Publisher.class);
        events=Mockito.mock(Events.class);
        webController = new WebController(players, ticker, eventPublisher,events);
    }

    @Test
    public void should_update_url_if_password_is_valid() {
        String NAME = "McCallum";
        Player existingPlayer = new Player(NAME, "password", "url");
        when(players.findByName(NAME)).thenReturn(Optional.of(existingPlayer));

        webController.registerPlayer(new PlayerRegistrationDto(NAME, "password", "new-url"));

        verify(players).update(existingPlayer);
        assertThat(existingPlayer.url()).isEqualTo("new-url");
    }

    @Test(expected = InvalidCredentialException.class)
    public void should_fail_to_update_url_if_password_is_invalid() {
        String NAME = "McCallum";
        Player existingPlayer = new Player(NAME, "password", "url");
        when(players.findByName(NAME)).thenReturn(Optional.of(existingPlayer));

        webController.registerPlayer(new PlayerRegistrationDto(NAME, "invalid-password", "new-url"));
    }
}
