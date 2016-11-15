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
import java.time.Duration
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

weight = 0.9 as double

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

    int nbDays = Duration.between(data.returnDate, data.departureDate).toDays()
    double total = totalForADay * nbDays
    total = applyRiskFactor(total, data.country)
    total = addOptions(total, data.travellerAges, data.options)
    total = applyCoverFactor(total, data.cover)
    total
}

public class QuestionInsuranceGenerator implements QuestionGenerator {
    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        Country country = randomizator.pickOne(Country.values(), { c -> c.populationInMillions() })
        LocalDate dpDate = LocalDate.now().plusDays(randomizator.randomInt(10))
        LocalDate reDate = dpDate.plusDays(randomizator.randomInt(45))
        int nbTraveller = randomizator.randomInt(5)
        int[] ages = randomizator.randomPositiveInts(nbTraveller, 95)
        List<Option> options = Option.values().findAll { o -> randomizator.randomDouble() < 0.2 }.toList()
        Cover cover = randomizator.pickOne(Cover.values())

        Data data = new Data(
                country: country,
                departureDate: dpDate,
                returnDate: reDate,
                travellerAges: ages,
                options: options,
                cover: cover)
        return new QuestionInsurance(data)
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

public class QuestionInsurance extends QuestionSupport<Data> implements Question<Data> {

    final Data data;

    public QuestionInsurance(Data data) {
        this.data = data
    }

    @Override
    Object questionData() {
        return data
    }

    @Override
    boolean accepts(@NotNull Question.Response response) {
        return response
                .get("quote", Double.class)
                .map({ double actual ->
            double expected = quote(data)
            Math.abs(expected - actual) < 1e-3
        })
                .orElse(false)
    }

    @Override
    Data expectedResponse() {
        return data;
    }
}
