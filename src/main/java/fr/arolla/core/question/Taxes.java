package fr.arolla.core.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static fr.arolla.core.question.Taxes.Country.*;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Taxes {

    public enum Country {
        DE,
        UK,
        FR,
        IT,
        ES,
        PL,
        RO,
        NL,
        BE,
        EL,
        CZ,
        PT,
        HU,
        SE,
        AT,
        BG,
        DK,
        FI,
        SK,
        IE,
        HR,
        LT,
        SI,
        LV,
        EE,
        CY,
        LU,
        MT
    }


    public static Taxes defaultTaxes() {
        Map<Country, TaxAndPopulation> m = new HashMap<>();
        m.put(DE, tp(1.2d, 190995));
        m.put(UK, tp(1.21d, 152741));
        m.put(FR, tp(1.2d, 151381));
        m.put(IT, tp(1.25d, 143550));
        m.put(ES, tp(1.19d, 109023));
        m.put(PL, tp(1.21d, 90574));
        m.put(RO, tp(1.2d, 46640));
        m.put(NL, tp(1.2d, 39842));
        m.put(BE, tp(1.24d, 26510));
        m.put(EL, tp(1.2d, 25338));
        m.put(CZ, tp(1.19d, 24755));
        m.put(PT, tp(1.23d, 24261));
        m.put(HU, tp(1.27d, 23141));
        m.put(SE, tp(1.23d, 23047));
        m.put(AT, tp(1.22d, 20254));
        m.put(BG, tp(1.21d, 16905));
        m.put(DK, tp(1.21d, 13348));
        m.put(FI, tp(1.17d, 12903));
        m.put(SK, tp(1.18d, 12767));
        m.put(IE, tp(1.21d, 10894));
        m.put(HR, tp(1.23d, 9952));
        m.put(LT, tp(1.23d, 6844));
        m.put(SI, tp(1.24d, 4858));
        m.put(LV, tp(1.2d, 4656));
        m.put(EE, tp(1.22d, 3094));
        m.put(CY, tp(1.21d, 2));
        m.put(LU, tp(1.25d, 1));
        m.put(MT, tp(1.2d, 1));
        return new Taxes(m);
    }

    private final Map<Country, TaxAndPopulation> m;

    public Taxes(Map<Country, TaxAndPopulation> m) {
        this.m = m;
    }

    public List<Country> countries() {
        return new ArrayList<>(m.keySet());
    }

    public Function<Double, Double> taxOf(Country country) {
        return m.get(country).tax;
    }

    private static TaxAndPopulation tp(double tax, int population) {
        return new TaxAndPopulation(tax, population);
    }

    public static class TaxAndPopulation {
        private final double defaultTax;
        private final Function<Double, Double> tax;
        private final int population;

        public TaxAndPopulation(double defaultTax, int population) {
            this.defaultTax = defaultTax;
            this.tax = x -> x * defaultTax;
            this.population = population;
        }
    }
}
