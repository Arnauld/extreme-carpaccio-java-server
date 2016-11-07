package fr.arolla.core;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Component
public class Ticker {
    private final AtomicInteger tick = new AtomicInteger();

    public int current() {
        return tick.get();
    }

    public int tick() {
        return tick.incrementAndGet();
    }
}
