package fr.arolla.util;

import java.io.File;
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

    public File getWorkingDirectory() {
        return new File(properties.getProperty("work.dir"));

    }

    public File getScriptDirectory() {
        return new File(properties.getProperty("script.dir"));
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
