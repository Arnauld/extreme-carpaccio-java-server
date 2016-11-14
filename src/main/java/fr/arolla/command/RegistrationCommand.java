package fr.arolla.command;

import fr.arolla.core.Event;
import fr.arolla.core.Player;
import fr.arolla.core.Players;
import fr.arolla.core.event.InvalidRegistrationEvent;
import fr.arolla.core.event.PlayerRegisteredEvent;
import fr.arolla.core.event.PlayerUrlUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RegistrationCommand {
    private final Logger log = LoggerFactory.getLogger(RegistrationCommand.class);
    private final Players players;
    private final Event.Publisher eventPublisher;
    private final int tick;
    private String username;
    private String password;
    private String url;

    public RegistrationCommand(Players players, Event.Publisher eventPublisher,int tick) {
        this.players = players;
        this.eventPublisher = eventPublisher;
        this.tick=tick;
    }

    public RegistrationCommand withUsername(String username) {
        this.username = username;
        return this;
    }

    public RegistrationCommand withPassword(String password) {
        this.password = password;
        return this;
    }

    public RegistrationCommand withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * @throws InvalidCredentialException
     * @throws InvalidParametersException
     */
    public void execute() {
        if (isBlank(username) || isBlank(password) || isBlank(url)) {
            log.warn("Invalid parameters '{}' / '{}': '{}'", username, hidePassword(password), url);
            eventPublisher.publish(new InvalidRegistrationEvent(username,tick, url));
            throw new InvalidParametersException();
        }

        Optional<Player> playerOpt = players.findByName(username);
        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            checkCredentials(player, password);
            player.changeUrl(url);
            players.update(player);
            eventPublisher.publish(new PlayerUrlUpdatedEvent(username,tick, url));
        } else {
            Player player = new Player(username, password, url);
            players.add(player);
            eventPublisher.publish(new PlayerRegisteredEvent(username,tick, url));
        }
    }


    private static String hidePassword(String password) {
        //noinspection ReplaceAllDot
        return password == null ? null : password.replaceAll(".", "x");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void checkCredentials(Player player, String password) {
        if (Objects.equals(player.password(), password))
            return;
        throw new InvalidCredentialException();
    }

}
