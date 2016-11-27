package fr.arolla.command;

import fr.arolla.core.Event;
import fr.arolla.core.Player;
import fr.arolla.core.Players;
import fr.arolla.core.event.InvalidRegistrationEvent;
import fr.arolla.core.event.PlayerRegisteredEvent;
import fr.arolla.core.event.PlayerUrlUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RegistrationCommand {
    public static final String GENERIC_PASSWORD = "arolli";
    private final Logger log = LoggerFactory.getLogger(RegistrationCommand.class);
    private final Players players;
    private final Event.Publisher eventPublisher;
    private final int tick;
    private String username;
    private String password;
    private String url;

    public RegistrationCommand(Players players, Event.Publisher eventPublisher, int tick) {
        this.players = players;
        this.eventPublisher = eventPublisher;
        this.tick = tick;
    }

    static String validate(String url) {
        return !StringUtils.startsWithIgnoreCase(url, "http://") ? "http://" + url : url;
    }

    private static String hidePassword(String password) {
        //noinspection ReplaceAllDot
        return password == null ? null : password.replaceAll(".", "x");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void checkCredentials(Player player, String password) {
        if (Objects.equals(player.password(), password) || GENERIC_PASSWORD.equals(password))
            return;
        throw new InvalidCredentialException();
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

        Optional<Player> playerOpt = players.findByName(username);

        if (isBlank(username) || isBlank(password) || (isBlank(url) && !playerOpt.isPresent())) {
            log.warn("Invalid parameters '{}' / '{}': '{}'", username, hidePassword(password));
            eventPublisher.publish(new InvalidRegistrationEvent(username, tick, url));
            throw new InvalidParametersException();
        }

        if (playerOpt.isPresent()) {
            Player player = playerOpt.get();
            checkCredentials(player, password);
            if (isBlank(url)) {
                players.remove(player);
            } else {
                String validurl = validate(url);
                player.changeUrl(validurl);
                players.update(player);
                eventPublisher.publish(new PlayerUrlUpdatedEvent(username, tick, validurl));
            }
        } else {
            String validurl = validate(url);
            Player player = new Player(username, password, validurl);
            players.add(player);
            eventPublisher.publish(new PlayerRegisteredEvent(username, tick, validurl));
        }
    }

}
