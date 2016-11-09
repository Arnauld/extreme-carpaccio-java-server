package fr.arolla.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Component
public class Randomizator {

    private final Random random;

    public Randomizator() {
        this(new SecureRandom());
    }

    public Randomizator(Random random) {
        this.random = random;
    }

    /**
     * @see Random#nextInt(int)
     */
    public int randomInt(int bound) {
        return random.nextInt(bound);
    }

    /**
     * @see Random#nextDouble()
     */
    public double randomDouble() {
        return random.nextDouble();
    }

    /**
     * @see Random#nextInt(int)
     */
    public int[] randomPositiveInts(int nb, int bound) {
        return IntStream.range(0, nb).map(i -> random.nextInt(bound)).toArray();
    }

    /**
     * @see Random#nextDouble()
     */
    public double[] randomPositiveDoubles(int nb, double bound) {
        return IntStream.range(0, nb).mapToDouble(i -> random.nextDouble() * bound).toArray();
    }

    public <T> T pickOne(T[] values) {
        return values[random.nextInt(values.length)];
    }

    public <T> T pickOne(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }

    public <T> T pickOne(List<T> values, ToDoubleFunction<T> weight) {
        double p = random.nextDouble();

        double total = values.stream().mapToDouble(weight).sum();
        double cumulativeProbability = 0.0d;
        for (T value : values) {
            cumulativeProbability += weight.applyAsDouble(value) / total;
            if (p <= cumulativeProbability)
                return value;
        }

        // ok, rely on basic random
        return pickOne(values);
    }

    public <T> T pickOne(T[] values, ToDoubleFunction<T> weight) {
        return pickOne(Arrays.asList(values), weight);
    }
}
