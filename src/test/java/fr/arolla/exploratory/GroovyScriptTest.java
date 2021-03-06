package fr.arolla.exploratory;

import fr.arolla.core.Question;
import fr.arolla.core.question.QuestionMultipleChoice;
import fr.arolla.core.question.ResponseSupport;
import fr.arolla.util.FileWatchr;
import fr.arolla.util.WorkDirectory;
import org.apache.commons.io.IOUtils;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class GroovyScriptTest {

    private static final Offset<Double> EPS = Offset.offset(1e-3);

    @Rule
    public WorkDirectory workDirectory = new WorkDirectory().deleteAfterwards(false);

    private ScriptEngine engine;
    private ScriptEngineManager manager;

    @Before
    public void setUp() {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("groovy");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_a_script__tax_case() throws ScriptException {
        engine.eval(new InputStreamReader(getClass().getResourceAsStream("/script/taxes.groovy")));
        List<Tax> taxes = (List<Tax>) engine.get("taxes");

        assertThat(taxes).hasSize(2);
        assertThat(taxes.get(0).country).isEqualTo("FR");
        assertThat(taxes.get(1).country).isEqualTo("UK");
        assertThat(taxes.get(0).taxFn.apply(100.0)).isCloseTo(50.0, EPS);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_a_script__and_not_fail_when_querying_a_missing_variable() throws ScriptException {
        engine.eval(new InputStreamReader(getClass().getResourceAsStream("/script/taxes.groovy")));

        // Attempt to retrieve a missing variable
        assertThat(engine.get("questions")).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_a_script__question_case() throws ScriptException {
        engine.eval(new InputStreamReader(getClass().getResourceAsStream("/script/question.groovy")));
        List<QuestionMultipleChoice> qs = (List<QuestionMultipleChoice>) engine.get("questions");

        assertThat(qs).hasSize(4);
        assertThat(qs.get(0).accepts(r("Belladona")).accepted()).isTrue();
        assertThat(qs.get(1).accepts(r("java")).accepted()).isTrue();
        assertThat(qs.get(1).accepts(r("c#")).accepted()).isFalse();
        assertThat(qs.get(2).accepts(r("YEs")).accepted()).isTrue();
        assertThat(qs.get(3).accepts(r("a binary associative operation, an identity element     ")).accepted()).isTrue();
    }

    @Test(expected = ScriptException.class)
    @SuppressWarnings("unchecked")
    public void should_fail_when_loading_an_invalid_script() throws ScriptException {
        engine.eval(new InputStreamReader(getClass().getResourceAsStream("/script/invalid.groovy")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_detect_a_script_change() throws Exception {
        File file = new File(workDirectory.dir(), "taxes.groovy");
        try (InputStream in = getClass().getResourceAsStream("/script/taxes.groovy");
             FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }

        engine.eval(new InputStreamReader(new FileInputStream(file)));

        List<Tax> taxes = (List<Tax>) engine.get("taxes");
        assertThat(taxes).hasSize(2);
        assertThat(taxes.get(0).country).isEqualTo("FR");
        assertThat(taxes.get(0).taxFn.apply(100.0)).isCloseTo(50.0, EPS);

        FileWatchr watchr = new FileWatchr(file);
        assertThat(watchr.hasChanged()).isFalse();
        assertThat(watchr.hasChanged()).isFalse();

        try (InputStream in = getClass().getResourceAsStream("/script/taxes2.groovy");
             FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
            out.flush();
        }

        await().atMost(5, TimeUnit.SECONDS).until(() ->
                assertThat(watchr.hasChanged()).describedAs("File change not detected...").isTrue());

        engine = manager.getEngineByName("groovy");
        engine.eval(new InputStreamReader(new FileInputStream(file)));

        taxes = (List<Tax>) engine.get("taxes");
        assertThat(taxes).hasSize(3);
        assertThat(taxes.get(0).country).isEqualTo("FR");
        assertThat(taxes.get(1).country).isEqualTo("UK");
        assertThat(taxes.get(2).country).isEqualTo("NL");
        assertThat(taxes.get(0).taxFn.apply(100.0)).isCloseTo(500.0, EPS);
        assertThat(taxes.get(1).taxFn.apply(100.0)).isCloseTo(30.0, EPS);
        assertThat(taxes.get(2).taxFn.apply(100.0)).isCloseTo(7.0, EPS);
    }


    private static Question.Response r(String content) {
        return new ResponseSupport("response", content);
    }

    private static Question.Response r(double content) {
        return new ResponseSupport("total", content);
    }

    private static Question.Response r() {
        return new ResponseSupport(Collections.emptyMap());
    }
}