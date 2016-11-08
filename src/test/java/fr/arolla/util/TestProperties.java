package fr.arolla.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class TestProperties {


    public static TestProperties get() {
        return new TestProperties().load();
    }

    private Properties properties;

    private TestProperties load() {
        this.properties = loadProperties();
        return this;
    }

    public String getWorkingDirectory() {
        return properties.getProperty("work.dir");

    }

    private Properties loadProperties() {
        String resourcePath = "/test.properties";
        try {
            Properties p = new Properties();
            p.load(new InputStreamReader(getClass().getResourceAsStream(resourcePath), "UTF8"));
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties " + resourcePath, e);
        }
    }
}
