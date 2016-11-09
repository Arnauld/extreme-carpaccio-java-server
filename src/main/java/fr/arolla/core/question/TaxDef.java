package fr.arolla.core.question;

import java.util.function.DoubleUnaryOperator;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class TaxDef {
    public final String country;
    public final DoubleUnaryOperator taxFn;

    public TaxDef(String country, DoubleUnaryOperator taxFn) {
        this.country = country;
        this.taxFn = taxFn;
    }
}
