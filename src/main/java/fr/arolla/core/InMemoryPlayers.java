package fr.arolla.core;

import fr.arolla.util.Sampler;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
//@Repository
public class InMemoryPlayers implements Players {

    private Map<String, Player> players = new ConcurrentHashMap<>();
    private Map<String, List<Double>> cashHistories = new ConcurrentHashMap<>();

    public static String keyOf(String name) {
        return name.toLowerCase();
    }

    private static String keyOf(Player player) {
        return player.username().toLowerCase();
    }

    @PostConstruct
    public void removeMe() {
        add(new Player("batman", "pwd", "http://localhost:4567"));
        add(new Player("superman", "pwd", "http://localhost:4567"));
        add(new Player("robin", "pwd", "http://localhost:4567"));
        add(new Player("xmen", "pwd", "http://localhost:4567"));
        add(new Player("wolverine", "pwd", "http://localhost:4567"));
        add(new Player("blanka", "pwd", "http://localhost:4567"));
        add(new Player("ken", "pwd", "http://localhost:4567"));
        add(new Player("ryu", "pwd", "http://localhost:4567"));
        add(new Player("mario", "pwd", "http://localhost:4567"));
        add(new Player("luigi", "pwd", "http://localhost:4567"));
        add(new Player("peach", "pwd", "http://localhost:4567"));
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
        update(player);
        resetCashHistory(player);
    }

    private List<Double> resetCashHistory(Player player) {

        Integer size = cashHistories.values().stream()
                .max((o1, o2) -> Integer.compare(o1.size(), o2.size()))
                .map(List::size)
                .orElse(1);
        ArrayList<Double> cash = new ArrayList<>(size);
        IntStream.range(0, size).forEach((x) -> cash.add(0d));

        return cashHistories.put(keyOf(player), cash);
    }

    @Override
    public Stream<CashHistory> sampledCashHistories(int sampling) {
        return cashHistories.entrySet()
                .stream()
                .map(e -> sampledCashHistory(sampling, players.get(e.getKey()), e.getValue()));
    }

    @Override
    public void addCash(String username, double amount) {
        List<Double> history = Optional.ofNullable(cashHistories.get(keyOf(username))).orElse(new ArrayList<>());
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

    @Override
    public void remove(Player player) {
        players.remove(player.username().toLowerCase());
        cashHistories.remove(keyOf(player));
    }

    @Override
    public void resetScore(String username) {
        Player playerToReset = players.get(username);
        playerToReset.cash(0);
        players.remove(playerToReset);
        this.add(playerToReset);
    }

    @Override
    public void saveState() {

    }

    public Map<String, List<Double>> getCashHistories() {
        return cashHistories;
    }

    public boolean isPlayersEmpty() {
        return players.isEmpty();
    }
}
