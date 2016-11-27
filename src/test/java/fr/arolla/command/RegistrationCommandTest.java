package fr.arolla.command;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by Domo-kun on 27/11/2016.
 */
public class RegistrationCommandTest {

    @Test
    public void url_starting_with_http() {
        String url = "http://123.456.789.012:1234";
        assertThat(RegistrationCommand.validate(url)).isEqualTo(url);
    }

    @Test
    public void url_starting_without_http() {
        String url = "http://123.456.789.012:1234";
        String urlWithout = "123.456.789.012:1234";
        assertThat(RegistrationCommand.validate(urlWithout)).isEqualTo(url);
    }
}