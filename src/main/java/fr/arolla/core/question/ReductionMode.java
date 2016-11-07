package fr.arolla.core.question;

import java.util.function.Function;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public enum ReductionMode {
    PayThePrice(t -> t),
    HalfPrice(t -> t / 2.0d),
    Standard(t -> {
        if (t >= 50_000)
            return (1 - 0.15) * t;
        if (t >= 10_0000)
            return (1 - 0.10) * t;
        if (t >= 7_0000)
            return (1 - 0.07) * t;
        if (t >= 5_0000)
            return (1 - 0.05) * t;
        if (t >= 1_0000)
            return (1 - 0.03) * t;
        return t;
    });

    private final Function<Double, Double> fn;

    ReductionMode(Function<Double, Double> fn) {
        this.fn = fn;
    }

    public double applyReduction(double total) {
        return fn.apply(total);
    }
}
