package fr.arolla.core;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryPlayersTest {

    private InMemoryPlayers players = new InMemoryPlayers();

    @Test
    public void should_reset_score__even_with_username_containing_uppercase_letter() {
        players.add(new Player("Luigi", "password", "http://somewh.ere"));
        players.addCash("Luigi", 100.0d);

        assertThat(players.findByName("Luigi").get().cash()).isEqualTo(100.0d);

        players.resetScore("Luigi");

        assertThat(players.findByName("Luigi").get().cash()).isEqualTo(0.0d);
    }
}