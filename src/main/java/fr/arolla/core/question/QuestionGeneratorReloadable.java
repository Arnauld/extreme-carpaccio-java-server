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
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class QuestionGeneratorReloadable implements QuestionGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(QuestionGeneratorReloadable.class);

    private final Randomizator randomizator;
    private final File scriptFile;
    private final Taxes taxes;
    private final FileWatchr fileWatchr;

    private List<TaxDef> taxDefs;
    private List<QuestionMultipleChoice> questions;

    public QuestionGeneratorReloadable(Randomizator randomizator, File scriptFile) {
        this(randomizator, scriptFile, Taxes.defaultTaxes());
    }

    public QuestionGeneratorReloadable(Randomizator randomizator, File scriptFile, Taxes taxes) {
        this.randomizator = randomizator;
        this.scriptFile = scriptFile;
        this.taxes = taxes;
        this.fileWatchr = new FileWatchr(scriptFile);
    }

    @Override
    public Question nextQuestion(int tick) {
        reloadConfigurationIfRequired();

        double p = randomizator.randomDouble();
        if (p < 0.3 && questions != null && !questions.isEmpty()) {
            return randomizator.pickOne(questions);
        }

        return randomPriceQuestion();
    }

    private Question randomPriceQuestion() {
        int sz = randomizator.randomInt(5);
        int[] quantities = randomizator.randomPositiveInts(sz, 10);
        double[] prices = randomizator.randomPositiveDoubles(sz, 100.0d);
        Taxes.Country country = randomizator.pickOne(taxes.countries(), Taxes.Country::populationInMillions);

        DoubleUnaryOperator taxFn = taxFn(country);

        ReductionMode reductionMode = randomizator.pickOne(ReductionMode.values());
        return new QuestionPriceCalculation(
                quantities, prices, country, reductionMode,
                taxFn,
                reductionMode::applyReduction);
    }

    private DoubleUnaryOperator taxFn(Taxes.Country country) {
        if (taxDefs != null) {
            return taxDefs.stream()
                    .filter(def -> def.country.equals(country.name()))
                    .findAny()
                    .map(def -> def.taxFn)
                    .orElseGet(() -> taxes.taxOf(country));
        }
        return taxes.taxOf(country);
    }

    @SuppressWarnings("unchecked")
    private void reloadConfigurationIfRequired() {
        if (!scriptFile.exists())
            throw new IllegalStateException("Script file does not exists '" + scriptFile.getAbsolutePath() + "'");

        if ((taxDefs == null && questions == null)
                || fileWatchr.hasChanged()) {

            LOG.info("Reloading configuration from {}", scriptFile.getAbsolutePath());

            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("groovy");

            try (InputStreamReader in = new InputStreamReader(new FileInputStream(scriptFile))) {
                engine.eval(in);
                taxDefs = (List<TaxDef>) engine.get("taxes");
                questions = (List<QuestionMultipleChoice>) engine.get("questions");
            } catch (ScriptException | IOException e) {
                throw new RuntimeException("Fail to load script '" + scriptFile.getAbsolutePath() + "'", e);
            }
        }
    }
}