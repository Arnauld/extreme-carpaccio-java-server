package fr.arolla.web;

import com.google.gson.Gson;
import fr.arolla.core.Event;
import fr.arolla.core.Player;
import fr.arolla.core.Players;
import fr.arolla.core.Ticker;
import fr.arolla.core.Events;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(WebController.class)
public class WebControllerIntegTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private Players players;

    @MockBean
    private Ticker ticker;

    @MockBean
    private Events events;

    @MockBean
    private Event.Publisher eventPublisher;

    @Test
    public void should_register_a_new_player() throws Exception {
        given(players.findByName(anyString()))
                .willReturn(Optional.<Player>empty());

        this.mvc.perform(post("/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(q("{'name':'carmen', 'password':'travis', 'url':'http://localhost:8080'}"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
        //        .andExpect(content().string("Honda Civic"))
        ;

        verify(players).add(any(Player.class));
    }

    @Test
    public void should_fail_to_register_an_existing_player_if_passwords_are_not_equals() throws Exception {
        Player player = new Player("carmen", "pacman", "http://anotherhost:9090");
        given(players.findByName(anyString()))
                .willReturn(Optional.of(player));

        this.mvc.perform(post("/seller")
                .contentType(MediaType.APPLICATION_JSON)
                .content(q("{'name':'carmen', 'password':'travis', 'url':'http://localhost:8080'}"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void should_fail_to_register_a_player_if_a_parameter_is_missing_or_empty() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "carmen");
        values.put("password", "travis");

        for (String key : values.keySet()) {

            Map<String, Object> copy = new HashMap<>(values);
            copy.remove(key);

            this.mvc.perform(post("/seller")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new Gson().toJson(copy))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError())
                    .andExpect(status().isBadRequest());
        }
    }

    private static String q(String s) {
        return s.replace('\'', '"');
    }
}
