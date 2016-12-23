package fr.arolla.core.question;

import fr.arolla.core.QuestionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Configuration
public class QuestionConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(QuestionConfiguration.class);

    private final boolean basicMode;
    private final String scriptPaths;

    @Autowired
    public QuestionConfiguration(@Value("${questionGenerator.basic:true}") boolean basicMode,
                                 @Value("${questionGenerator.scriptPaths:}") String scriptPaths) {
        this.basicMode = basicMode;
        this.scriptPaths = scriptPaths;
    }

    @Bean
    public QuestionGenerator questionGenerator() {
        QuestionGeneratorBasic generatorBasic = new QuestionGeneratorBasic();
        if (basicMode) {
            LOG.info("Question generator in priceWithTax mode");
            return generatorBasic;
        } else {
            QuestionGeneratorComposite composite = new QuestionGeneratorComposite();
            Stream.of(scriptPaths.split(","))
                    .map(File::new)
                    .map(this::listFolder).flatMap(Collection::stream)
                    .map(this::listFolder).flatMap(Collection::stream)
                    .map(this::logGeneratorFile)
                    .map(QuestionGeneratorScriptBased::new)
                    .forEach(q -> composite.register(q, q));

            LOG.info("Composite Question generator based on path {}", scriptPaths);
            return wrapWithFallback(composite, generatorBasic);
        }
    }

    private List<File> listFolder(File path) {
        List<File> files = new ArrayList<>();
        if (path.isFile()) {
            files.add(path);
        }
        if (path.isDirectory()) {
            Arrays.stream(path.list()).map(name->path.getAbsolutePath().concat("/").concat(name)).map(File::new).forEach(files::add);
        }
        return files;
    }

    private File logGeneratorFile(File file) {
        LOG.info("Question generator based on {}", file.getAbsolutePath());
        return file;
    }

    private static QuestionGenerator wrapWithFallback(QuestionGenerator primary, QuestionGenerator secondary) {
        return (tick, randomizator) -> {
            try {
                return primary.nextQuestion(tick, randomizator);
            } catch (Exception e) {
                LOG.warn("Fail to generate question for tick {}, fallback on secondary generator", tick, e);
            }
            return secondary.nextQuestion(tick, randomizator);
        };
    }
}
