//
// Three fields need to be defined and will be retrieved from loading the script
//
// * version
// * weight
// * generator
//
import com.fasterxml.jackson.annotation.JsonFormat
import fr.arolla.core.Question
import fr.arolla.core.QuestionGenerator
import fr.arolla.core.question.Country
import fr.arolla.util.Randomizator

import javax.validation.constraints.NotNull
import java.time.LocalDate
// ----------------------------------------------------------------------------
//
// VERSION
//
// ----------------------------------------------------------------------------

// hint field to check in log if your configuration have been reloaded

version = "1.0.0"

// ----------------------------------------------------------------------------
//
// WEIGHT
//
// ----------------------------------------------------------------------------

weight = 0.0 as double

// ----------------------------------------------------------------------------
//
// INSURANCE
//
// ----------------------------------------------------------------------------

// Sorry, winter sports cover is not available for trips that are longer than 31 days.
public enum Option {
    WinterSports,
    SportsAndActivities,
    MedicalConditions
}

public enum Cover {
    Basic,
    Extra,
    Premier
}


public class InvalidQuestionInsuranceGenerator implements QuestionGenerator {
    private Random RANDOM = new Random()

    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        Object data = generateDatas(randomizator)
        return new InvalidTQuestionInsurance(data)
    }

    protected Object generateDatas(Randomizator randomizator) {

        Country country = randomizator.pickOne(Country.values(), { c -> c.populationInMillions() })
        LocalDate dpDate = LocalDate.now().plusDays(randomizator.randomInt(10))
        LocalDate reDate = dpDate.plusDays(randomizator.randomInt(45))
        int nbTraveller = randomizator.randomInt(5)+3
        int[] ages = randomizator.randomPositiveInts(nbTraveller, 95)
        List<Option> options = Option.values().findAll { o -> randomizator.randomDouble() < 0.2 }.toList()
        Cover cover = randomizator.pickOne(Cover.values())
        String countryString=country.name();

        switch(RANDOM.nextInt(9)){
            case 0 :
                // date de depart au siecle dernier
                dpDate = LocalDate.now().minusYears(randomizator.randomInt(100)+500)
                reDate = LocalDate.now().plusDays(randomizator.randomInt(45))
                break;
            case 1 :
            case 2 :
                //date depart apres la date d'arrivée
                reDate = LocalDate.now().plusDays(randomizator.randomInt(10))
                dpDate = reDate.plusDays(randomizator.randomInt(45)+5)
                ;break;
            case 3 :
                //age negatif
                def index = RANDOM.nextInt(ages.length)
                ages[index]=-ages[index]
                break;
            case 4 :
                //ages en lettres
                Object[] invalidAges = randomizator.randomPositiveInts(nbTraveller, 95)
                invalidAges[RANDOM.nextInt(invalidAges.length)]=randomizator.pickOne("twelve","one","five","16354080 minutes since your birth","354 months, and 3 days","272568 hours since your birth","seventy moons","3 cars","two","twenty","forty-six","yellow submarine");
                return new InvalidTravellersAgesData(
                        country: country,
                        departureDate: dpDate,
                        returnDate: reDate,
                        travellerAges: invalidAges,
                        options: options,
                        cover: cover)
                break;
            case 5 :
                countryString=country.frenchName().reverse().toLowerCase().replace("e","3")
                break;
            case 6 :
                countryString=country.frenchName().substring(3).toLowerCase().replace("a","4")
                break;
            default:
                countryString=randomizator.randomDouble()

        }

        Data data = new Data(
                country: countryString,
                departureDate: dpDate,
                returnDate: reDate,
                travellerAges: ages,
                options: options,
                cover: cover)
        data
    }
}

generator = new InvalidQuestionInsuranceGenerator()

public class Data {
    String country;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    int[] travellerAges;
    List<Option> options;
    Cover cover;
}

public class InvalidTravellersAgesData {
    String country;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    Object[] travellerAges;
    List<Option> options;
    String cover;
}

public class InvalidTQuestionInsurance implements Question {

    final Object data;

    public InvalidTQuestionInsurance(Object data) {
        this.data = data
    }

    @Override
    Object questionData() {
        return data
    }

    @Override
    boolean accepts(@NotNull Question.Response response) {
        return false
    }

    @Override
    boolean isInvalid() {
        return true
    }

    @Override
    double gainAmount() {
        return 400
    }

    @Override
    double lossPenalty() {
        return 400
    }

    @Override
    Object expectedResponse() {
        return null
    }
}
