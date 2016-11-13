package fr.arolla.core;

import fr.arolla.util.Sampler;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Repository
public class InMemoryPlayers implements Players {

    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, List<Double>> cashHistories = new ConcurrentHashMap<>();

    @PostConstruct
    public void removeMe() {
        add(new Player("batman", "pwd", "http://localhost:4566"));
        add(new Player("ruby", "pwd", "http://localhost:4567"));
        add(new Player("jhondoe", "pwd", "http://localhost:4568"));
        add(new Player("ruppert", "pwd", "http://localhost:4569"));
    }

    @Override
    public Stream<Player> all() {
        return players.values().stream().sorted(Comparator.comparing(Player::username));
    }

    @Override
    public Optional<Player> findByName(String name) {
        return Optional.ofNullable(players.get(keyOf(name)));
    }

    @Override
    public void update(Player player) {
        players.put(keyOf(player), player);
    }

    @Override
    public void add(Player player) {
        ArrayList<Double> cash = new ArrayList<>();
        cash.add(0.0d);

        players.put(keyOf(player), player);
        cashHistories.put(keyOf(player), cash);
    }

    @Override
    public Stream<CashHistory> sampledCashHistories(int sampling) {
        return cashHistories.entrySet()
                .stream()
                .map(e -> sampledCashHistory(sampling, players.get(e.getKey()), e.getValue()));
    }

    @Override
    public void addCash(String username, double amount) {
        List<Double> history = cashHistories.get(keyOf(username));
        Double last = history.get(history.size() - 1);
        history.add(last + amount);
        players.get(keyOf(username)).cash(last + amount);
    }

    private CashHistory sampledCashHistory(int sampling, Player player, List<Double> values) {
        double[] sample = new Sampler().sample(sampling, values);
        return new CashHistory(player, sample);
    }

    @Override
    public void markPlayerOnline(String username, boolean online) {
        players.get(keyOf(username)).setOnline(online);
    }

    private static String keyOf(String name) {
        return name.toLowerCase();
    }

    private static String keyOf(Player player) {
        return player.username().toLowerCase();
    }
}
