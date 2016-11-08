package fr.arolla.util;

import fr.arolla.util.Sampler;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SamplerTest {

    private Sampler sampler = new Sampler();

    @Test
    public void should_have_at_least_one_element_if_provided_values_is_not_empty() {
        List<Double> values = IntStream.rangeClosed(0, 1)
                .mapToObj(i -> (double) i)
                .collect(Collectors.toList());

        double[] sample = sampler.sample(10, values);
        assertThat(sample).containsExactly(0.0d);
    }

    @Test
    public void should_sample_values() {
        List<Double> values = IntStream.rangeClosed(0, 99)
                .mapToObj(i -> (double) i)
                .collect(Collectors.toList());

        double[] sample = sampler.sample(10, values);
        assertThat(sample).containsExactly(0.0d, 10.0d, 20.0d, 30.0d, 40.0d, 50.0d, 60.0d, 70.0d, 80.0d, 90.0d);
    }

    @Test
    public void should_sample_values_last_element_in() {
        List<Double> values = IntStream.rangeClosed(0, 100)
                .mapToObj(i -> (double) i)
                .collect(Collectors.toList());

        double[] sample = sampler.sample(10, values);
        assertThat(sample).containsExactly(0.0d, 10.0d, 20.0d, 30.0d, 40.0d, 50.0d, 60.0d, 70.0d, 80.0d, 90.0d, 100.0d);
    }
}