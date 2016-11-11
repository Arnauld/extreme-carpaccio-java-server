package fr.arolla.core.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static fr.arolla.core.question.Country.*;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Taxes {


    public static Taxes defaultTaxes() {
        Map<Country, Double> m = new HashMap<>();
        m.put(DE, 1.2d);
        m.put(UK, 1.21d);
        m.put(FR, 1.2d);
        m.put(IT, 1.25d);
        m.put(ES, 1.19d);
        m.put(PL, 1.21d);
        m.put(RO, 1.2d);
        m.put(NL, 1.2d);
        m.put(BE, 1.24d);
        m.put(EL, 1.2d);
        m.put(CZ, 1.19d);
        m.put(PT, 1.23d);
        m.put(HU, 1.27d);
        m.put(SE, 1.23d);
        m.put(AT, 1.22d);
        m.put(BG, 1.21d);
        m.put(DK, 1.21d);
        m.put(FI, 1.17d);
        m.put(SK, 1.18d);
        m.put(IE, 1.21d);
        m.put(HR, 1.23d);
        m.put(LT, 1.23d);
        m.put(SI, 1.24d);
        m.put(LV, 1.2d);
        m.put(EE, 1.22d);
        m.put(CY, 1.21d);
        m.put(LU, 1.25d);
        m.put(MT, 1.2d);
        return new Taxes(m);
    }

    private final Map<Country, Double> m;

    public Taxes(Map<Country, Double> m) {
        this.m = m;
    }

    public List<Country> countries() {
        return new ArrayList<>(m.keySet());
    }

    public DoubleUnaryOperator taxOf(Country country) {
        return x -> x * m.get(country);
    }
}
