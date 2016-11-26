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
public class QuestionGeneratorScriptBased implements QuestionGenerator, HasWeight {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionGeneratorScriptBased.class);

    private final File scriptFile;
    private final FileWatchr fileWatchr;
    //
    private int checkedTick = -1;
    //
    private QuestionGenerator generator;
    private double weight;

    public QuestionGeneratorScriptBased(File scriptFile) {
        this.scriptFile = scriptFile;
        this.fileWatchr = new FileWatchr(scriptFile);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOrDefault(ScriptEngine engine, String key, T defaultValue) {
        Object v = engine.get(key);
        if (v == null)
            return defaultValue;
        return (T) v;
    }

    @Override
    public double weight(int tick) {
        reloadConfigurationIfRequired(tick);
        return weight;
    }

    @Override
    public Question nextQuestion(int tick, Randomizator randomizator) {
        reloadConfigurationIfRequired(tick);
        return generator.nextQuestion(tick, randomizator);
    }

    private void reloadConfigurationIfRequired(int tick) {
        if (checkedTick == tick)
            return;
        checkedTick = tick;

        if (!scriptFile.exists())
            throw new IllegalStateException("Script file does not exists '" + scriptFile.getAbsolutePath() + "'");

        if (generator == null || fileWatchr.hasChanged()) {

            LOG.info("\n" +
                    "******************\n" +
                    "******************\n" +
                    "******************\n" +
                    "******************\n" +
                    "******************\n" +
                    "******************\n" +
                    "Reloading configuration from {}", scriptFile.getAbsolutePath());

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("groovy");

            try (InputStreamReader in = new InputStreamReader(new FileInputStream(scriptFile))) {
                engine.eval(in);
                generator = (QuestionGenerator) engine.get("generator");
                weight = getOrDefault(engine, "weight", 0.5);

                String version = getOrDefault(engine, "version", "?");
                LOG.info("Configuration reloaded, version {}", version);

            } catch (ScriptException | IOException e) {
                throw new RuntimeException("Fail to load script '" + scriptFile.getAbsolutePath() + "'", e);
            } catch (ClassCastException e) {
                throw new RuntimeException("Invalid script type '" + scriptFile.getAbsolutePath() + "'", e);
            }
        }
    }

}
