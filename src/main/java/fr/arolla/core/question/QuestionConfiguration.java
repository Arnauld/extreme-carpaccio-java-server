package fr.arolla.core.question;

import fr.arolla.core.QuestionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Configuration
public class QuestionConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(QuestionConfiguration.class);

    private final boolean basicMode;
    private final File scriptFile;

    @Autowired
    public QuestionConfiguration(@Value("${questionGenerator.basic:true}") boolean basicMode,
                                 @Value("${questionGenerator.scriptPath:}") File scriptFile) {
        this.basicMode = basicMode;
        this.scriptFile = scriptFile;
    }

    @Bean
    public QuestionGenerator questionGenerator() {
        QuestionGeneratorBasic generatorBasic = new QuestionGeneratorBasic();
        if (basicMode) {
            LOG.info("Question generator in basic mode");
            return generatorBasic;
        } else {
            LOG.info("Question generator in advanced mode using script at '{}'", scriptFile);
            return wrapWithFallback(new QuestionGeneratorScriptBased(scriptFile), generatorBasic);
        }
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
