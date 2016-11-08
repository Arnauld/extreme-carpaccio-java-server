package fr.arolla.exploratory;

import java.util.function.Function;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Tax {
    public final String country;
    public final Function<Double, Double> taxFn;

    public Tax(String country, Function<Double, Double> taxFn) {
        this.country = country;
        this.taxFn = taxFn;
    }
}
