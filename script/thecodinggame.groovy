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

weight = 0.5 as double

// ----------------------------------------------------------------------------
//
// INSURANCE
//
// ----------------------------------------------------------------------------

public enum Cover {
	Basic(1.1),// on doit avoir forcément au moins un Cover présent
	Extra(0.3),
	Premier(0.3)

	public double rate;

	private Cover(double rate){
		this.rate=rate;
	}

	public double rate() {
		return rate;
	}
}

public enum Option {
	Skiing(0.2),
	Medical(0.3),
	Scuba(0.1),
	Sports(0.3),
	Yoga(0.1)

	public double rate;

	private Option(double rate){
		this.rate=rate;
	}

	public double rate() {
		return rate;
	}
}

public class QuestionInsurance extends QuestionSupport implements Question {

	Data data

	@Override
	Data questionData() {
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
						Math.abs(expected - actual.doubleValue()) < 1e-3
					})
					.orElse(false), { -> String.format("%.2f", expected) })

		return Question.ResponseValidation.rejected("Missing property 'quote' of type Double = "+data.quote);
	}

    @Override
    double lossErrorPenalty() {
        return -50d
    }

    @Override
    double lossOfflinePenalty() {
        return -50d
    }

    @Override
    double lossPenalty() {
        return -50d
    }

    @Override
    double gainAmount() {
        return 100d
    }
}



public class QuestionInsuranceGenerator implements QuestionGenerator {

	def Map merge(Map... maps) {
		Map result
		if (maps.length == 0) {
			result = [:]
		} else if (maps.length == 1) {
			result = maps[0]
		} else {
			result = [:]
			maps.each { map ->
				map.each { k, v ->
					result[k] = result[k] instanceof Map ? merge(result[k], v) : v
				}
			}
		}
		result
	}
	
	// Phase 1 of the game (simple discovery with simple formula using just traveller count)
	def phase1(){
		def config = [
			"coverPrices": [
				(Cover.Basic): 1.8
			].asImmutable(),
			"countriesRisks": [
				(Country.ES): 1.0,
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
	
	// Phase 2 of the game (Slicing with the basic rules)
	def phase2(){
		def config = phase1()
		def changes = [
			"coverPrices": [
				(Cover.Extra): 2.4,
				(Cover.Premier): 4.2
			].asImmutable(),
			"countriesRisks": [
				(Country.ES): 1.0,
				(Country.BE): 1.1,
				(Country.FI): 0.8,
				(Country.FR): 1.0,
				(Country.EL): 0.9
			].asImmutable(),
			"ageRisks": [
				(0): 0.5,
				(18): 1.0,
				(46): 1.2,
				(66): 1.4,
				(76): 2.0
			].asImmutable(),
		].asImmutable()
		config = merge(config, changes)
		config
	}
	
	// phase 3 of the game, with frequent changes of the pricing formula at each step
	def phase3(int step){
		def previous = phase2()
		def config = previous
	
		// should apply one change about every 10mn
		def changes = [
			["coverPrices": [(Cover.Basic): 1.7].asImmutable()].asImmutable(),
			["countriesRisks": [(Country.UK): 1.1].asImmutable()].asImmutable(),
			["countriesRisks": [(Country.CZ): 1.4].asImmutable()].asImmutable(),
			["optionsPrices": [(Option.Skiing): 28].asImmutable()].asImmutable(),
			["ageRisks": [(86): 2.2].asImmutable()].asImmutable(),
			["optionsPrices": [(Option.Medical): 72].asImmutable()].asImmutable(),
			["countriesRisks": [(Country.IT): 1.2].asImmutable()].asImmutable(),
			["ageRisks": [(76): 1.8].asImmutable()].asImmutable(),
			["countriesRisks": [(Country.EL): 1.1].asImmutable()].asImmutable(),
			["constraint": ["minimum": 19].asImmutable()].asImmutable(),
			["optionsPrices": [(Option.Sports): 25].asImmutable()].asImmutable(),
			["optionsPrices": [(Option.Scuba): 36].asImmutable()].asImmutable(),
			["optionsPrices": [(Option.Yoga): -3].asImmutable()].asImmutable(),
		].asImmutable().subList(0, step)
		changes.forEach{change -> config = merge(config, change)}
		config
	}
	
	// -------------- TESTS --------------
	
	def testAll(){
		testAgeRisk()
		testSumOfRiskAdjustedAges()
		testPhases()
		testStandardQuote()
	}
	
	def testPhases(){
		def config = phase1();
		assert config["coverPrices"].size() == 1
		assert config["countriesRisks"].size() == 3
	
		config = phase2();
		assert config["ageRisks"].size() == 5
		assert config["coverPrices"].size() == 3
	
		config = phase3(0);
		assert config["ageRisks"].size() == 5
		assert config["coverPrices"].size() == 3
	
		config = phase3(1);
		assert config["countriesRisks"].size() == 5
	
		config = phase3(2);
		assert config["countriesRisks"].size() == 6
		config = phase3(3);
		assert config["countriesRisks"].size() == 7
		config = phase3(7);
		assert config["countriesRisks"].size() == 8
	}
	
	
	
	def ageRisk(Double age, Map<Integer, Double> ageRisks){
		if (age == 0) return ageRisks.get(0)
		def level = ageRisks.keySet().findAll{it <= age}.max()
		ageRisks.get(level) as double
	}
	
	def testAgeRisk(){
		def ageRisks = [
			(0): 0.5,
			(18): 1.0,
			(46): 1.2,
			(66): 1.4,
			(76): 2.0
		].asImmutable()
		assert ageRisk(0, ageRisks) == 0.5
		assert ageRisk(17, ageRisks) == 0.5
		assert ageRisk(18, ageRisks) == 1.0
		assert ageRisk(45, ageRisks) == 1.0
		assert ageRisk(46, ageRisks) == 1.2
		assert ageRisk(66, ageRisks) == 1.4
		assert ageRisk(76, ageRisks) == 2.0
		assert ageRisk(100, ageRisks) == 2.0
	
		def flatAgeRisks = [
			(0): 1.0,
		].asImmutable()
		assert ageRisk(0, flatAgeRisks) == 1.0
		assert ageRisk(100, flatAgeRisks) == 1.0
	}
	
	def sumOfRiskAdjustedAges(int[] travellerAges, Map<Integer, Double> ageRisks){
		double totalForADay = travellerAges
				.collect({ age -> ageRisk(age, ageRisks) }) // collect == map
				.inject(0, { sum, price -> sum + price }) as double // inject  == reduce/fold
		return totalForADay
	}
	
	def testSumOfRiskAdjustedAges(){
		def ageRisks = [
			(0): 0.5,
			(18): 1.0,
			(46): 1.2,
			(66): 1.4,
			(76): 2.0
		].asImmutable()
		assert sumOfRiskAdjustedAges([] as int[], ageRisks) == 0
		assert sumOfRiskAdjustedAges([18] as int[], ageRisks) == 1.0 // solo
		assert sumOfRiskAdjustedAges([24, 27] as int[], ageRisks) == 2.0 // couple
		assert sumOfRiskAdjustedAges([24, 27, 3] as int[], ageRisks) == 2.5 // family with 1 kid
		assert sumOfRiskAdjustedAges([24, 27, 3, 7] as int[], ageRisks) == 3.0 // family with 2 kids
		assert sumOfRiskAdjustedAges([66, 68] as int[], ageRisks) == 2.8 // senior couple
	}
	
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

	def toTravellers(int[] travellersAge, Country country) {
		TypoPassenger[] passengers = new TypoPassenger[travellersAge.length];
		int[] am = getAgeMapping(country)
		int ageToBeAdult=am[0]
		int ageToBeSenior=am[1]

		for(int index=0;index<travellersAge.length;index++){
			if(travellersAge[index]<ageToBeAdult){
				passengers[index]=TypoPassenger.CHILD
			}else if(travellersAge[index]<ageToBeSenior) {
				passengers[index]=TypoPassenger.ADULT
			}else{
				passengers[index]=TypoPassenger.SENIOR
			}
		}
		passengers

	}

	private getAgeMapping(Country country) {
		switch (country) {
		//case Country.FR:
			case Country.MT:
				return [18, 60]

//          case Country.IT:
//          case Country.NL:
//			case Country.UK:
			case Country.LU:
				return [15, 60]
//            case Country.ES:
//			case Country.EL:
			case Country.EE:
//            case Country.PT:
				return [12, 65]

//			case Country.CZ:
			case Country.CY:
				//          case Country.LT:
				//          case Country.SI:
				//          case Country.LV:
				//          case Country.RO:
				//          case Country.BG:
				return [15, 55]
			default:
				return [18, 65]
		}
	}

	def quote(Data data, Map config) {

		TravelData travel = toTravelData(data)

		double price = coverPrice(data.cover, config["coverPrices"])
		double sumOfAges = sumOfRiskAdjustedAges(data.travellerAges, config["ageRisks"])
		double countryRisk = countryRisk(data.country, config["countriesRisks"])

		double optionPrice = optionPrice(data.options, config["optionsPrices"])
		int nbDays = travel.nbDays

		//au delà de 3 semaines,on ne facture que les semaines pleines
		if(((int)travel.nbDays/7)>=3){
			nbDays=((int)travel.nbDays/7)*7
		}

		double total = price * countryRisk * sumOfAges * nbDays + optionPrice

		if(false){
			int nbChilds=travel.travellers.findAll { t -> TypoPassenger.CHILD }.toList().size()
			int nbAdults=travel.travellers.findAll { t -> TypoPassenger.ADULT }.toList().size()
			int nbSeniors=travel.travellers.findAll { t -> TypoPassenger.SENIOR }.toList().size()
			//réduction pack famille
			if(nbChilds==2 && nbAdults==2){
				total-=(total*20/100)
			}

			//seniors qui encadrent des enfants=> risque
			if(nbAdults==0){
				total+=(total*5/100)
			}
			//malus trop de personnes agées
			if(nbSeniors>(nbAdults+nbChilds)){
				total+=(total*nbSeniors/100)
			}
			//pas assez d'adultes encadrants
			if(nbChilds-nbAdults>0){
				total+=(total*15/100)
			}
		}
		total
	}

	private TravelData toTravelData(Data data) {
		TravelData travel = new TravelData(
				country: data.country,
				nbDaysToDeparture: LocalDates.nbDaysBefore(LocalDate.now(), data.departureDate),
				nbDays: LocalDates.nbDaysBefore(data.departureDate, data.returnDate),
				cover: data.cover,
				travellers: toTravellers(data.travellerAges, data.country),
				options: data.options
		)
		travel
	}

	def testStandardQuote(){
		def config = phase2();
		println config
	
		1.upto(20, {
			def data = generateData(new Randomizator(), config)
			println data
			def quote = quote(data, config)
			println quote
		})
	}
	
	def Data generateData(Randomizator randomizator, Map config) {
		def availableCovers = config["coverPrices"].keySet() as Cover[]
		def availableCountries = config["countriesRisks"].keySet() as Country[]
		def availableOptions = config["optionsPrices"].keySet() as List<Option>
	
		Cover cover = randomizator.pickOne(availableCovers, { c -> c.rate() })
		Country country = randomizator.pickOne(availableCountries, { c -> c.populationInMillions() })
		LocalDate dpDate = LocalDate.now().plusDays(randomizator.randomInt(10))
		LocalDate reDate = dpDate.plusDays(randomizator.randomInt(45))
		int nbTraveller = randomizator.randomInt(4) + 1
		int[] ages = randomizator.randomPositiveInts(nbTraveller, 95)
		List<Option> options = availableOptions.findAll { o -> randomizator.randomDouble() < o.rate }.toList()
		
		Country originCountry = randomizator.pickOne(Country.values(), { c -> c.populationInMillions() })
		Data data = new Data(
				country: country,
				departureDate: dpDate,
				returnDate: reDate,
				travellerAges: ages,
				options: options,
				cover: cover,
				quote: 0)
		double quote = quote(data, config)
		data.quote = quote
		data
	}
/*
	@Override
	Question nextQuestion(int tick, Randomizator randomizator) {
		def config = phase1() // CHANGE HERE THE PHASE & STEP OF THE GAME
		Data data = generateData(randomizator, config)
		return new QuestionInsurance(data: data)
	}
*/
	@Override
	Question nextQuestion(int tick, Randomizator randomizator) {
		def config = phase3(10)
		Data data = generateData(randomizator, config)
		return new 	QuestionInsuranceCrossSelling(data: data,travelData: toTravelData(data))
	}

}

generator = new QuestionInsuranceGenerator()

@ToString(includeNames=true)
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
	//Country originCountry;// for dataviz
}


public class TravelData {
	Country country;
	int nbDaysToDeparture;
	int nbDays;
	TypoPassenger[] travellers;
	List<Option> options;
	Cover cover;
}

public enum TypoPassenger {
	CHILD, ADULT, SENIOR
}

public class QuestionInsuranceCrossSelling extends QuestionSupport implements Question {

	Data data
	TravelData travelData

	double penalty = -50d
	private double gains = 100d


	@Override
	Data questionData() {
		return data
	}

	@Override
	Question.ResponseValidation accepts(@NotNull Question.Response response) {
		String expected = "good cross selling proposals"
		Optional<List<String>> offersOpt = response.get("offers", List.class);
		if (offersOpt.isPresent())
			return Question.ResponseValidation
					.of(offersOpt
					.map({ List<String> actual ->
				reward(travelData, actual)
			}).map({ r -> checkReward(r) }).orElse(false), { -> expected })

		return Question.ResponseValidation.rejected("Missing property 'offers' of type String arrays = " + data.quote);
	}

	private boolean checkReward(reward) {
		if (reward <= 0) {
			penalty = reward;
			return false;
		} else {
			gains=reward
			return true;
		}
	}

	@Override
	double lossErrorPenalty() {
		return penalty
	}

	@Override
	double lossOfflinePenalty() {
		return penalty
	}

	@Override
	double lossPenalty() {
		return penalty
	}

	@Override
	double gainAmount() {
		return gains
	}


	def reward(TravelData data, List<String> offers) {

		// Reward score much more than regular: 300 instead of 100

		def REWARD = 300.0;

		// Abusing the cross-selling offers and you loose the customer trust completely

		if (offers.size > 4) {

			return -100.0

		}

		// Too many cross-selling offers and you sell none

		if (offers.size >= 3) {

			return 0

		}

		// Basic Cover is price-sensitive, only takes "free" offers

		if (data.cover == Cover.Basic) {

			if(offers.any { offer -> containsAny(offer, ["free"]) }){
				return REWARD/3
			}

		}

		// Departure in a a few days? You may need a visa / passport express

		if (data.nbDaysToDeparture < 21 && offers.any { offer ->

			containsAny(offer, ["visa", "passport"])
		}) {

			return REWARD

		}

		// Senior or Cover.Premier enjoy luguage transfer and shuttle or valet

		if ((data.cover == Cover.Premier || data.travellers.contains(TypoPassenger.SENIOR))

				&& offers.any { offer ->

			containsAny(offer, [

					"luguage",

					"baggage",

					'transfer',

					"shuttle"

			])
		}) {

			return REWARD

		}

		// Travelling with kids, may need a baby sitter or child care

		if (data.travellers.contains(TypoPassenger.CHILD) && offers.any { offer ->

			containsAny(offer, [

					"baby sitter",

					"baby sitting",

					"babysitting",

					"babysitter",

					"child care",

					"day care",

					"child wellfare",

					"nursery"

			])
		}) {

			return REWARD

		}

		// Long term travel is like expat

		if (data.nbDays > 90 && offers.any { offer ->

			containsAny(offer, [

					"moving",

					"expat",

					"relocation"

			])
		}) {

			return REWARD

		}

		// USA (MISSING COUNTRY) needs ESTA

		if (data.country == Country.UK && data.nbDaysToDeparture < 21 && offers.any { offer ->

			containsAny(offer, ["esta",])
		}) {

			return REWARD

		}

		// USA (MISSING COUNTRY) is the country of cars

		if (data.country == Country.UK && offers.any { offer ->

			containsAny(offer, [

					"car rental",

					"car insurance"

			])
		}) {

			return REWARD

		}

		//option = Skiing  -> "mountain" "guide" "wine" "pass" "restaurant" "club" "VIP" only if country = CH

		if (data.options.contains(Option.Skiing) && data.cover == Cover.Premier && offers.any { offer ->

			containsAny(offer, [

					"vip",

					"restaurant",

					"helicopter",

					"off piste",

					"off-piste",

					"mountain guide",

					"club"

			])
		}) {

			return REWARD

		}

		// Everybody loves apps

		if (offers.any { offer ->

			containsAny(offer, [

					"mobile",

					"ios",

					"android",

					"iphone",

					"ipad"

			])
		}) {

			return REWARD

		}

		// Everybody loves tourist guides

		if (offers.any { offer ->

			containsAny(offer, [

					"lonely planet",

					"tourism guide",

					"tourist guide",

					"traveller guide",

					"touristic guide",

					"tourist map",

					"tourism map",

					"traveller map",

					"tourist info",

					"travel info"

			])
		}) {

			return REWARD

		}

		0

	}
	def containsAny(String token, List<String> keywords){

		keywords.any { token.toLowerCase().contains( it)}

	}

}

//---------------TESTS---------------

def test_containsAny(){

	assert containsAny("mobile app", ["mobile", "ios"])

	assert containsAny("mobile app", ["app"])

	assert containsAny("expat magazine", ["expat"])

}

def testAll(){

	test_containsAny();

	def REWARD = 300.0;

	TravelData data = new TravelData(

			country: Country.BE,

			nbDaysToDeparture: 22,

			nbDays: 10,

			travellers: [],

			options: [],

			cover: Cover.Basic)

	assert reward(data, [

			"bla",

			"bla",

			"bla",

			"bla",

			"bla",

			"bla"

	]) == -100.0

	assert reward(data, ["bla", "bla", "bla"]) == 0.0

	data.cover  = Cover.Extra

	assert reward(data, ["mobile app"]) == REWARD

	data.nbDaysToDeparture = 7

	assert reward(data, ["visa"]) == REWARD

	data.nbDays = 92

	data.nbDaysToDeparture = 30

	assert reward(data, ["expat magazine"]) == REWARD

	data.nbDays = 14

	data.cover = Cover.Premier

	assert reward(data, ["airport transfer"]) == REWARD

	assert reward(data, ["airport shuttle"]) == REWARD

	assert reward(data, ["luguage delivery"]) == REWARD

}