package fr.arolla.core.question;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.XcServerConfiguration;
import fr.arolla.core.Question;
import fr.arolla.util.Randomizator;
import fr.arolla.util.TestProperties;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionGeneratorScriptBasedTest {

    private Randomizator randomizator = new Randomizator();
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        XcServerConfiguration conf = new XcServerConfiguration();
        objectMapper = conf.configureMapper();
    }

    @Test
    public void insurance_usecase() throws JsonProcessingException {
        File scriptFile = new File(TestProperties.get().getScriptDirectory(), "insurance.groovy");
        QuestionGeneratorScriptBased generator = new QuestionGeneratorScriptBased(scriptFile);

        Question question = generator.nextQuestion(1, randomizator);
        String json = objectMapper.writeValueAsString(question.questionData());
        assertThat(json)
                .containsPattern("\"country\":\"[A-Z]{2}\"")
                .containsPattern("\"departureDate\":\"\\d{4}\\-\\d{2}\\-\\d{2}\"")
                .containsPattern("\"returnDate\":\"\\d{4}\\-\\d{2}\\-\\d{2}\"")
                .containsPattern("\"travellerAges\":\\[(\\d+,?)*\\]")
                .containsPattern("\"options\":\\[(\"(MedicalConditions|SportsAndActivities|WinterSports)\",?)*\\]")
                .containsPattern("\"cover\":\"(Premier|Basic|Extra)\"");
    }

}