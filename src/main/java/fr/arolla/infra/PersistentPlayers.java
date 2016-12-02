package fr.arolla.infra;

import fr.arolla.core.CashHistory;
import fr.arolla.core.InMemoryPlayers;
import fr.arolla.core.Player;
import fr.arolla.core.Players;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Domo-kun on 25/11/2016.
 */
@Repository
public class PersistentPlayers implements Players {

    private static final String FIELD_SEPARATOR = ";";
    private static final String DATA_SEPARATOR = "===";
    private static final String PATH_SEPARATOR = "/";
    private static final String INTRADATA_SEPARATOR = "###";
    private static final String PLAYERS_FILE = "players";
    private static final String CASH_FILE = "cash";
    private final Logger log = LoggerFactory.getLogger(PersistentPlayers.class);

    private InMemoryPlayers inMemoryPlayers = new InMemoryPlayers();

    @Value("${data.persistentPath:/tmp/}")
    private String path;

    private boolean loaded = false;

    //@PostConstruct
    public void removeMe() {
        inMemoryPlayers.removeMe();
    }


    @Override
    public Stream<Player> all() {
        if (!loaded && inMemoryPlayers.isPlayersEmpty()) {
            loadState();
        }
        return inMemoryPlayers.all();
    }

    @Override
    public Optional<Player> findByName(String name) {
        return inMemoryPlayers.findByName(name);
    }

    @Override
    public void update(Player player) {
        inMemoryPlayers.update(player);
        saveState();
    }

    @Override
    public void add(Player player) {
        inMemoryPlayers.add(player);
        saveState();
    }

    @Override
    public Stream<CashHistory> sampledCashHistories(int sampling) {
        return inMemoryPlayers.sampledCashHistories(sampling);
    }

    @Override
    public void addCash(String username, double amount) {
        inMemoryPlayers.addCash(username, amount);
    }

    @Override
    public void markPlayerOnline(String username, boolean online) {
        inMemoryPlayers.markPlayerOnline(username, online);
    }

    @Override
    public void remove(Player player) {
        inMemoryPlayers.remove(player);
        saveState();
    }

    @Override
    public void resetScore(String username) {
        inMemoryPlayers.resetScore(username);
        saveState();
    }

    @Override
    public void saveState() {
        persist(PLAYERS_FILE, inMemoryPlayers.all(), this::serialize);
        persist(CASH_FILE, inMemoryPlayers.getCashHistories().entrySet().stream(), this::serialize);
    }

    @Override
    public void resetAllScore() {
        inMemoryPlayers.resetAllScore();
        saveState();
    }

    private void loadState() {
        load(PLAYERS_FILE, this::unserializePlayers);
        load(CASH_FILE, this::unserializeCash);
        loaded = true;
    }

    private void unserializeCash(String s) {
        if (!s.isEmpty()) {
            String[] fields = s.split(FIELD_SEPARATOR);
            List<Double> cashHistory = Stream.of(fields[1].split(INTRADATA_SEPARATOR)).
                    map(Double::valueOf).
                    collect(Collectors.toList());
            String playerName = fields[0];
            inMemoryPlayers.getCashHistories().put(InMemoryPlayers.keyOf(playerName), cashHistory);
        }
    }

    private void unserializePlayers(String s) {
        if (!s.isEmpty()) {
            String[] fields = s.split(FIELD_SEPARATOR);
            String playerName = fields[0];
            String password = fields[1];
            Boolean online = Boolean.getBoolean(fields[2]);
            String url = fields[3];
            Double cash = Double.valueOf(fields[4]);
            Player p = new Player(playerName, password, url);
            p.setOnline(online);
            p.cash(cash);
            inMemoryPlayers.update(p);
        }
    }

    private void load(String fileName, Consumer<String> unserializer) {
        try (Stream<String> stream = Files.readAllLines(createValidPath(fileName)).stream()) {
            String data = stream.collect(Collectors.joining());
            if (!data.isEmpty()) {
                log.debug("load data {} for parsing", data);
                Stream.of(data.split(DATA_SEPARATOR)).forEach(unserializer);
            }
            log.info("{} loaded", fileName);
        } catch (NoSuchFileException nsfe){
            log.warn("no persis file {}", fileName);
        } catch (IOException e) {
            log.error("ERROR while trying to read {}", fileName, e);
        }
    }

    private <T> void persist(String fileName, Stream<T> stream, Function<T, String> serializer) {
        String data = stream
                .map(serializer)
                .collect(Collectors.joining(DATA_SEPARATOR));
        try {
            Files.write(createValidPath(fileName), data.getBytes());
            log.trace("{} saved", fileName);
        } catch (IOException e) {
            log.error("ERROR while trying to persist {}", fileName, e);
        }
    }

    private Path createValidPath(String fileName) {
        if (StringUtils.endsWithIgnoreCase(path, PATH_SEPARATOR)) {
            return Paths.get(path + fileName);
        }
        return Paths.get(path + PATH_SEPARATOR + fileName);
    }

    private String serialize(Map.Entry<String, List<Double>> cashEntry) {
        return cashEntry.getKey() + FIELD_SEPARATOR +
                cashEntry.getValue().stream().sequential()
                        .map(Object::toString)
                        .collect(Collectors.joining(INTRADATA_SEPARATOR));
    }

    private String serialize(Player p) {
        return p.username() + FIELD_SEPARATOR +
                p.password() + FIELD_SEPARATOR +
                p.isOnline() + FIELD_SEPARATOR +
                p.url() + FIELD_SEPARATOR +
                p.cash();
    }
}
