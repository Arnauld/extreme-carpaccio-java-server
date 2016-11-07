package fr.arolla.util;

import org.assertj.core.api.Condition;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomizatorTest {

    private SecureRandom random = new SecureRandom();
    private Randomizator randomizator = new Randomizator();

    @Test
    public void should_generate_int_within_zero_and_the_upper_range() {
        int bound = random.nextInt(1000);
        for (int i = 0; i < 5000; i++) {
            assertThat(randomizator.randomInt(bound))
                    .isLessThan(bound)
                    .isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    public void should_generate_array_of_specified_ints_within_zero_and_the_upper_range() {
        int bound = random.nextInt(1000);
        int nb = random.nextInt(1000);
        for (int i = 0; i < 100; i++) {
            assertThat(randomizator.randomPositiveInts(nb, bound))
                    .hasSize(nb)
                    .has(allIntsWithin(0, bound));
        }
    }

    @Test
    public void should_generate_array_of_specified_doubles_within_zero_and_the_upper_range() {
        double bound = random.nextDouble() * 1000.0;
        int nb = random.nextInt(1000);
        for (int i = 0; i < 100; i++) {
            assertThat(randomizator.randomPositiveDoubles(nb, bound))
                    .hasSize(nb)
                    .has(allDoublesWithin(0, bound));
        }
    }

    @Test
    public void should_pick_one_randomly__list_case() {
        List<String> xs = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
        Set<String> found = new HashSet<>();

        for (int i = 0; i < 500; i++) {
            found.add(randomizator.pickOne(xs));
        }

        assertThat(found)
                .hasSize(xs.size());
    }

    @Test
    public void should_pick_one_randomly__array_case() {
        String[] xs = new String[]{"a", "b", "c", "d", "e", "f", "g"};
        Set<String> found = new HashSet<>();

        for (int i = 0; i < 500; i++) {
            found.add(randomizator.pickOne(xs));
        }

        assertThat(found)
                .hasSize(xs.length);
    }

    private static Condition<int[]> allIntsWithin(int lower, int upperExclusive) {
        return new Condition<>(
                (int[] arr) -> IntStream.of(arr).allMatch(i -> i >= lower && i < upperExclusive),
                "Integer in range [" + lower + ",  " + upperExclusive + "[");
    }


    private static Condition<double[]> allDoublesWithin(double lower, double upperExclusive) {
        return new Condition<>(
                (double[] arr) -> DoubleStream.of(arr).allMatch(i -> i >= lower && i < upperExclusive),
                "Integer in range [" + lower + ",  " + upperExclusive + "[");
    }

}