package fr.arolla.core.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import static fr.arolla.core.question.Taxes.Country.*;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Taxes {

    public enum Country {
        DE("Allemagne", 82.16),
        FR("France", 66.66),
        UK("Royaume-Uni", 65.34),
        IT("Italie", 60.67),
        ES("Espagne", 46.44),
        PL("Pologne", 37.97),
        RO("Roumanie", 19.76),
        NL("Pays-Bas", 16.98),
        BE("Belgique", 11.20),
        EL("Grèce", 10.79),
        CZ("République Tchèque", 10.51),
        PT("Portugal", 10.34),
        HU("Hongrie", 9.83),
        SE("Suède", 9.85),
        AT("Autriche", 8.70),
        BG("Bulgarie", 7.24),
        DK("Danemark", 5.62),
        FI("Finlande", 5.49),
        SK("Slovaquie", 5.43),
        IE("Irlande", 4.66),
        HR("Croatie", 4.19),
        LT("Lituanie", 2.89),
        SI("Slovénie", 2.06),
        LV("Lettonie", 1.97),
        EE("Estonie", 1.32),
        CY("Chypre", 0.85),
        LU("Luxembourg", 0.58),
        MT("Malte", 0.43);

        private final String name;
        private final double populationInMillions;

        Country(String name, double populationInMillions) {
            this.name = name;
            this.populationInMillions = populationInMillions;
        }

        public double populationInMillions() {
            return populationInMillions;
        }
    }


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
