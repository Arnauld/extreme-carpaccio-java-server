package fr.arolla.core.question;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
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

    private final String frenchName;
    private final double populationInMillions;

    Country(String name, double populationInMillions) {
        this.frenchName = name;
        this.populationInMillions = populationInMillions;
    }

    public double populationInMillions() {
        return populationInMillions;
    }

    public String frenchName() {
        return frenchName;
    }
}
