//
// Three fields need to be defined and will be retrieved from loading the script
//
// * version
// * weight
// * generator
//
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import fr.arolla.core.Question
import fr.arolla.core.QuestionGenerator
import fr.arolla.core.model.LocalDates
import fr.arolla.core.question.Country
import fr.arolla.core.question.QuestionSupport
import fr.arolla.util.Randomizator
import groovy.transform.ToString

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

weight = 1.0 as double

// ----------------------------------------------------------------------------
//
// INSURANCE
//
// ----------------------------------------------------------------------------

public enum Cover {
    Basic,
    Extra,
    Premier
}

public enum Option {
    Skiing,
    Medical,
    Scuba,
    Sports,
    Yoga
}


public class QuestionInsuranceGenerator1 implements QuestionGenerator {

	// Phase 1 of the game (simple discovery with simple formula using just traveller count)
	def phase1(){
		def config = [
			"coverPrices": [
				(Cover.Basic): 1.8
			].asImmutable(),
			"countriesRisks": [
                    (Country.FR): 1.0,
				    (Country.BE): 1.0,
				    (Country.FI): 1.0
			].asImmutable(),
			"ageRisks": [
				(0): 1.0 // traveller count
			].asImmutable(),
			"optionsPrices": [
				(Option.Skiing): 24
			].asImmutable()
		].asImmutable()
		assert config["ageRisks"].size() == 1
		config
	}

	// -------------- TESTS --------------

	def coverPrice(Cover cover, Map<Cover, Double> coverPrices){
		coverPrices[cover]
	}

	def countryRisk(Country country, Map<Country, Double> countryRisks){
		countryRisks[country]
	}

	def optionPrice(List<Option> options, Map<Option, Double> optionPrices) {
		options
				.collect({option -> optionPrices[option]})
				.inject(0, { sum, price -> sum + price })
	}

	def quote(Data data, Map config) {
		double price = coverPrice(data.cover, config["coverPrices"])
		double sumOfAges = data.travellerAges.size()
		double countryRisk = countryRisk(data.country, config["countriesRisks"])
		long nbDays = LocalDates.nbDaysBefore(data.departureDate,data.returnDate)
		double optionPrice  = optionPrice(data.options, config["optionsPrices"])
		double total = price * countryRisk * sumOfAges * nbDays + optionPrice
		total
	}

	def Data generateData(Randomizator randomizator, Map config) {

        LocalDate dpDate = LocalDate.now().plusDays(3)

        Data data1 = requestData(Country.FR, dpDate, dpDate.plusDays(2), randomizator.randomPositiveInts(1, 95), [], Cover.Basic)
        Data data2 = requestData(Country.BE, dpDate, dpDate.plusDays(1), randomizator.randomPositiveInts(2, 95), [], Cover.Basic)
        Data data3 = requestData(Country.FR, dpDate, dpDate.plusDays(randomizator.randomInt(5)+3), randomizator.randomPositiveInts(1, 95), [], Cover.Basic)

        Data data4 = requestData(Country.FI, dpDate, dpDate.plusDays(1), randomizator.randomPositiveInts(2, 95), [Option.Skiing], Cover.Basic)


        Data data = randomizator.pickOne([data1,data2,data3,data4])
//DECOMMENTER  data = randomizator.pickOne([data1,data2,data3,data4])

        double quote = quote(data, config)
		data.quote = quote
		data
	}

    private Data requestData(Country country, LocalDate dpDate, LocalDate reDate, int[] ages, List<Option> options, Cover cover) {
        Data data = new Data(
                country: country,
                departureDate: dpDate,
                returnDate: reDate,
                travellerAges: ages,
                options: options,
                cover: cover,
                quote: 0)
        data
    }


    @Override
	Question nextQuestion(int tick, Randomizator randomizator) {
		def config = phase1()
        Data data = generateData(randomizator, config)
		return new QuestionInsurance(data: data)
	}
}

generator = new QuestionInsuranceGenerator1()

@ToString(includeNames=true)
public class QuestionInsurance extends QuestionSupport implements Question {

    Data data

    @Override
    Object questionData() {
        return data
    }

    @Override
    Question.ResponseValidation accepts(@NotNull Question.Response response) {
        double expected = data.quote
        Optional<Number> valueOpt = response.get("quote", Number.class);
        if (valueOpt.isPresent())
            return Question.ResponseValidation
                    .of(valueOpt
                    .map({ Number actual ->
                Math.abs(expected - actual.doubleValue()) < 1e-2
            })
                    .orElse(false), { -> String.format("%.2f", expected) })

        return Question.ResponseValidation.rejected("Missing property 'quote' of type Double = "+data.quote);
    }

    @Override
    double lossErrorPenalty() {
        return -0d
    }

    @Override
    double lossOfflinePenalty() {
        return -0d
    }

    @Override
    double lossPenalty() {
        return -0d
    }

    @Override
    double gainAmount() {
        return 50d
    }
}

@ToString(includeNames = true)
public class Data {
    Country country;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate departureDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    int[] travellerAges;
    List<Option> options;
    Cover cover;
    @JsonIgnore
    double quote;
}