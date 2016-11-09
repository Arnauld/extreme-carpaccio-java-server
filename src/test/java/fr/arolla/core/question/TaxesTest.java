package fr.arolla.core.question;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class TaxesTest {

    @Test
    public void should_have_a_tax_for_each_country() {
        Taxes taxes = Taxes.defaultTaxes();

        SoftAssertions assertions = new SoftAssertions();
        for (Taxes.Country country : Taxes.Country.values()) {
            assertions.assertThat(taxes.taxOf(country)).describedAs("Missing tax for " + country).isNotNull();
        }

        assertions.assertAll();
    }
}