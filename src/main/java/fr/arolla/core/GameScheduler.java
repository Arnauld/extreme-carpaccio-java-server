package fr.arolla.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Component
public class GameScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(Game.class);
    private final Ticker ticker;

    private ScheduledExecutorService executorService;
    private final Game game;

    @Autowired
    public GameScheduler(Game game, Ticker ticker) {
        this(game, ticker, newExecutor());
    }

    public GameScheduler(Game game, Ticker ticker, ScheduledExecutorService executorService) {
        this.game = game;
        this.ticker = ticker;
        this.executorService = executorService;
    }

    private static ScheduledExecutorService newExecutor() {
        return Executors.newScheduledThreadPool(1, new ThreadFactory() {
            private final AtomicInteger idGen = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                ThreadGroup group = new ThreadGroup("Game-Threads") {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        LOG.error("Ooops in thread '{}'", t.getName(), e);
                    }
                };
                return new Thread(group, r, "Game-Thread-" + idGen.incrementAndGet());
            }
        });
    }

    @PostConstruct
    public void schedule() {
        executorService.scheduleAtFixedRate(() -> {
            int tick = ticker.tick();
            game.processIteration(tick);
        }, 5, 5, TimeUnit.SECONDS);
    }
}
