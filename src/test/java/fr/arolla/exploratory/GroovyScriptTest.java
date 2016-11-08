package fr.arolla.exploratory;

import fr.arolla.util.WorkDirectory;
import org.apache.commons.io.IOUtils;
import org.assertj.core.data.Offset;
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

    @Test
    @SuppressWarnings("unchecked")
    public void should_load_a_script() throws ScriptException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("groovy");
        engine.eval(new InputStreamReader(getClass().getResourceAsStream("/script/taxes.groovy")));
        List<Tax> taxes = (List<Tax>) engine.get("taxes");

        assertThat(taxes).hasSize(2);
        assertThat(taxes.get(0).country).isEqualTo("FR");
        assertThat(taxes.get(1).country).isEqualTo("UK");
        assertThat(taxes.get(0).taxFn.apply(100.0)).isCloseTo(50.0, EPS);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_detect_a_script_change() throws Exception {
        File file = new File(workDirectory.dir(), "taxes.groovy");
        try (InputStream in = getClass().getResourceAsStream("/script/taxes.groovy");
             FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out);
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("groovy");
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
}