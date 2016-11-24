package fr.arolla.core;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Players {
    Stream<Player> all();

    Optional<Player> findByName(String name);

    void update(Player player);

    void add(Player player);

    Stream<CashHistory> sampledCashHistories(int sampling);

    void addCash(String username, double amount);

    void markPlayerOnline(String username, boolean online);

    void remove(Player player);
}