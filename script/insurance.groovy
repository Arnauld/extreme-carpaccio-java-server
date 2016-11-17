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
import fr.arolla.core.question.QuestionSupport
import fr.arolla.util.Randomizator

import javax.validation.constraints.NotNull
import java.time.LocalDate
import java.time.Period

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

weight = 0.9 as double

// ----------------------------------------------------------------------------
//
// INSURANCE
//
// ----------------------------------------------------------------------------

// Sorry, winter sports cover is not available for trips that are longer than 31 days.
public enum Option {
    WinterSports(0.2),
    SportsAndActivities(0.0),
    MedicalConditions(0.0)
    public double rate;

    private Option(double rate){
        this.rate=rate;
    }
}

public enum Cover {
    Basic(1.1),// on doit avoir forcément au moins un Cover présent
    Extra(0.3),
    Premier(0.3)

    public double rate;

    private Cover(double rate){
        this.rate=rate;
    }

}

public class QuestionInsurance extends QuestionSupport implements Question {

    Data data

    def priceForAge(int age) {
        return 66.0;
    }

    def applyCoverFactor(double total, Cover cover) {
        switch (cover) {
            case Cover.Basic:
                return total

            case Cover.Extra:
                return total * 1.4

            case Cover.Premier:
                return total * 1.8
        }
    }

    def applyRiskFactor(double total, Country country) {
        switch (country) {
            case Country.UK:
                return total * 1.2

            case Country.DE:
            case Country.FR:
            case Country.IT:
            case Country.ES:
                return total * 1.0

            case Country.PL:
            case Country.RO:
                return total * 1.2

            case Country.NL:
            case Country.BE:
                return total * 1.1

            case Country.EL:
                return total * 0.9

            case Country.CZ:
                return total * 1.4

            case Country.PT:
                return total * 1.1

            case Country.HU:
                return total * 1.4

            case Country.SE:
            case Country.AT:
            case Country.BG:
            case Country.DK:
            case Country.FI:
            case Country.SK:
            case Country.IE:
                return total + 500

            case Country.HR:
            case Country.LT:
            case Country.SI:
            case Country.LV:
            case Country.EE:
            case Country.CY:
            case Country.LU:
                return total * 1.25

            case Country.MT:
            default:
                return total * 1.1
        }
    }

    def priceForOption(Option option, int age) {
        switch (option) {
            case Option.WinterSports:
                if (age < 5)
                    return 0;
                return 40
            case Option.SportsAndActivities:
                if (age < 5)
                    return 0
                return 35
            case Option.MedicalConditions:
                if (age < 5)
                    return 20
                return 30
        }
    }

    def addOptions(double total, int[] travellerAges, List<Option> options) {
        options.inject(total, { t0, option ->
            travellerAges.inject(t0, { t1, age -> t1 + priceForOption(option, age) })
        }) as double
    }

    def quote(Data data) {
        double totalForADay = data
                .travellerAges
                .collect({ d -> priceForAge(d) }) // collect == map
                .inject(0, { total, price -> total + price }) // inject  == reduce/fold

        int nbDays = Period.between(data.returnDate, data.departureDate).getDays()
        double total = totalForADay * nbDays
        total = applyRiskFactor(total, data.country)
        total = addOptions(total, data.travellerAges, data.options)
        total = applyCoverFactor(total, data.cover)
        total
    }

    @Override
    Data questionData() {
        return data
    }

    @Override
    Question.ResponseValidation accepts(@NotNull Question.Response response) {
        Optional<Double> valueOpt = response.get("quote", Double.class);
        if (valueOpt.isPresent())
            return Question.ResponseValidation
                    .of(valueOpt
                    .map({ double actual ->
                double expected = quoteFn(data)
                Math.abs(expected - actual) < 1e-3
            })
                    .orElse(false), { -> String.format("Expected: %.2f but got: %.2f", quoteFn(data), valueOpt.get()) })

        return Question.ResponseValidation.rejected("Missing property 'quote' of type Double");
    }
}


public class QuestionInsuranceGenerator implements QuestionGenerator {

    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        Data data = generateDatas(randomizator)
        return new QuestionInsurance(data: data)
    }

    protected Data generateDatas(Randomizator randomizator) {
        Country country = randomizator.pickOne(Country.values(), { c -> c.populationInMillions() })
        LocalDate dpDate = LocalDate.now().plusDays(randomizator.randomInt(10))
        LocalDate reDate = dpDate.plusDays(randomizator.randomInt(45))
        int nbTraveller = randomizator.randomInt(5)
        int[] ages = randomizator.randomPositiveInts(nbTraveller, 95)
        List<Option> options = Option.values().findAll { o -> randomizator.randomDouble() < o.rate }.toList()

        Cover cover = randomizator.pickOne(Cover.values().findAll { c -> randomizator.randomDouble() < c.rate }.toList())

        Data data = new Data(
                country: country,
                departureDate: dpDate,
                returnDate: reDate,
                travellerAges: ages,
                options: options,
                cover: cover)
        data
    }
}

generator = new QuestionInsuranceGenerator()

public class Data {
    Country country;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    int[] travellerAges;
    List<Option> options;
    Cover cover;
}

