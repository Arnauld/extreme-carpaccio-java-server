package fr.arolla.core.question;

import fr.arolla.core.Question;
import fr.arolla.core.QuestionGenerator;
import fr.arolla.util.FileWatchr;
import fr.arolla.util.Randomizator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionGeneratorReloadable implements QuestionGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionGeneratorReloadable.class);

    private final File scriptFile;
    private final FileWatchr fileWatchr;
    private QuestionGenerator generator;

    public QuestionGeneratorReloadable(File scriptFile) {
        this.scriptFile = scriptFile;
        this.fileWatchr = new FileWatchr(scriptFile);
    }

    @Override
    public Question nextQuestion(int tick, Randomizator randomizator) {
        reloadConfigurationIfRequired();
        return generator.nextQuestion(tick, randomizator);
    }

    private void reloadConfigurationIfRequired() {
        if (!scriptFile.exists())
            throw new IllegalStateException("Script file does not exists '" + scriptFile.getAbsolutePath() + "'");

        if (generator == null || fileWatchr.hasChanged()) {

            LOG.info("Reloading configuration from {}", scriptFile.getAbsolutePath());

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("groovy");

            try (InputStreamReader in = new InputStreamReader(new FileInputStream(scriptFile))) {
                engine.eval(in);
                generator = (QuestionGenerator) engine.get("generator");

                String version = getOrDefault(engine, "version", "?");
                LOG.info("Configuration reloaded, version {}", version);

            } catch (ScriptException | IOException e) {
                throw new RuntimeException("Fail to load script '" + scriptFile.getAbsolutePath() + "'", e);
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid script type '" + scriptFile.getAbsolutePath() + "'", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOrDefault(ScriptEngine engine, String key, T defaultValue) {
        Object v = engine.get(key);
        if (v == null)
            return defaultValue;
        return (T) v;
    }

}
