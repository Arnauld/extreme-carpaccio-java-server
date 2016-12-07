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

import static TypoPassenger.*

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
    Basic(1.1),// on doit avoir forcément au moins un Cover présent
    Extra(0.3),
    Premier(0.3)

    public double rate;

    private Cover(double rate) {
        this.rate = rate;
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

    private Option(double rate) {
        this.rate = rate;
    }

    public double rate() {
        return rate;
    }
}

@ToString(includeNames = true)
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
                Math.abs(expected - actual.doubleValue()) < 1e-2
            })
                    .orElse(false), { -> String.format("%.2f", expected) })

        return Question.ResponseValidation.rejected("Missing property 'quote' of type Double = " + data.quote);
    }

    @Override
    double lossErrorPenalty() {
        return -50d
    }

    @Override
    double lossOfflinePenalty() {
        return -25d
    }

    @Override
    double lossPenalty() {
        return -75d
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

    // carpaccio = full options/full country/full covers
    def carpaccio() {
        def config = [
                "coverPrices"   : [
                        (Cover.Basic)  : 1.8,
                        (Cover.Extra)  : 2.4,
                        (Cover.Premier): 4.2
                ].asImmutable(),
                "countriesRisks": [
                        (Country.DE): 0.8,
                        (Country.FR): 1.0,
                        (Country.UK): 1.1,
                        (Country.IT): 1.2,
                        (Country.ES): 1.1,
                        (Country.PL): 1.4,
                        (Country.RO): 1.3,
                        (Country.NL): 0.7,
                        (Country.EL): 0.6,
                        (Country.CZ): 1.2,
                        (Country.PT): 0.5,
                        (Country.HU): 1.1,
                        (Country.SE): 1.2,
                        (Country.AT): 0.9,
                        (Country.BE): 0.9,
                        (Country.BG): 1.1,
                        (Country.DK): 1.2,
                        (Country.FI): 0.8,
                        (Country.SK): 0.7,
                        (Country.IE): 1.1,
                        (Country.HR): 1.3,
                        (Country.LT): 0.7,
                        (Country.SI): 0.8,
                        (Country.LV): 0.6,
                        (Country.EE): 1.3,
                        (Country.CY): 1.6,
                        (Country.LU): 1.3,
                        (Country.MT): 1.2,

                ].asImmutable(),
                "optionsPrices" : [
                        (Option.Skiing) : 24,
                        (Option.Medical): 72,
                        (Option.Scuba)  : 36,
                        (Option.Yoga)   : -3,
                        (Option.Sports) : 25
                ].asImmutable()
        ].asImmutable()
        config
    }

/*
         See https://en.wikipedia.org/wiki/Roman_numerals

         Let's consider only the subset of Roman numerals up to L:
         Symbol 	I 	V 	X 	L
         Value 		1 	5 	10 	50

         Some examples:
         Number 	1	2	3	4	7	9	14 	40	88
         Notation 	I	II	III	IV 	VII	IX 	XIV	XL	LXXXVIII

         Let's consider a price by numerals:
         Symbol 	I 	V 	X 	L
         Price 		1 	4.4 8.4	39

         To calculate the price we convert the number of days to its Roman Numerals representation,
         then we turn each symbol into its corresponding price and add them together,
         except when there is subtractive notation such as IV or IX, in which case we substract the value.

         Examples
         Number	Numeral	Price
         1		I		1
         3		III		3*1
         4		IV		(4.4 - 1)
         5		V		4.4
         7		VII		4.4 + 2*1
         9		IX		(8.4 - 1)
         14		XIV		8.4 + (4.4 -1)
         39		XXXIX	3*8.4 + (8.4 -1)
         41		XLI		(39 - 8.4) +1
         77 	LXXVII	39 + 2*8.4 + 4.4 + 2*1
         */
    /* ACTIVAE FOR IT4 */

    def romanPrice(int n) {
        if (n >= 50) {
            return 39 + romanPrice(n - 50)
        }
        if (n >= 40) {
            return 39 - romanPrice(10) + romanPrice(n - 40)
        }
        if (n >= 9) {
            return 8.4 + romanPrice(n - 10)
        }
        if (n >= 4) {
            return 4.4 + (n - 5)
        }
        return n
    }

    def ageRisk(TypoPassenger typo) {
        switch (typo) {
            case CHILD: return 1.1 as double
            case YOUNG: return 0.9 as double
            case ADULT: return 1.0 as double
            case SENIOR: return 1.5 as double
            default: return 1.0 as double
        }
    }

    def sumOfRiskAdjustedAges(TypoPassenger[] travellers) {
        double totalForADay = travellers
                .collect({ age -> ageRisk(age) }) // collect == map
                .inject(0, { sum, price -> sum + price }) as double // inject  == reduce/fold
        return totalForADay
    }

    def coverPrice(Cover cover, Map<Cover, Double> coverPrices) {
        coverPrices[cover]
    }

    def countryRisk(Country country, Map<Country, Double> countryRisks) {
        countryRisks[country]
    }

    def optionPrice(List<Option> options, Map<Option, Double> optionPrices) {
        options
                .collect({ option -> optionPrices[option] })
                .inject(0, { sum, price -> sum + price })
    }

    def toTravellers(int[] travellersAge, Country country) {
        TypoPassenger[] passengers = new TypoPassenger[travellersAge.length];
        int[] am = getAgeMapping(country)
        int ageToBeYoung = am[0]
        int ageToBeAdult = am[1]
        int ageToBeSenior = am[2]

        for (int index = 0; index < travellersAge.length; index++) {
            if (travellersAge[index] < ageToBeYoung) {
                passengers[index] = CHILD
            } else if (travellersAge[index] < ageToBeAdult) {
                passengers[index] = YOUNG
            } else if (travellersAge[index] < ageToBeSenior) {
                passengers[index] = ADULT
            } else {
                passengers[index] = SENIOR
            }
        }
        passengers

    }

    private getAgeMapping(Country country) {
        switch (country) {
        //case Country.FR:
//			case Country.MT:
//				return [18,27, 60]

//          case Country.IT:
//          case Country.NL:
//			case Country.UK:
//			case Country.LU:
//				return [15,24, 60]
//            case Country.ES:
//			case Country.EL:
//			case Country.EE:
//            case Country.PT:
//				return [12,18, 65]

//			case Country.CZ:
//			case Country.CY:
        //          case Country.LT:
        //          case Country.SI:
        //          case Country.LV:
        //          case Country.RO:
        //          case Country.BG:
//				return [15,21, 55]
            default:
                return [18, 25, 65]
        }
    }

    def quote(Data data, Map config) {

        def phase3On = true
        /* ACTIVATE FOR IT4 */
        def phase4On = false
        TravelData travel = toTravelData(data)

        double price = coverPrice(data.cover, config["coverPrices"])
        double passengersRisks = sumOfRiskAdjustedAges(travel.travellers)
        double countryRisk = countryRisk(data.country, config["countriesRisks"])

        double optionPrice = optionPrice(data.options, config["optionsPrices"])
        int nbDays = travel.nbDays

        if (!phase4On) {
            //premiere semaine est indivisible
            if (nbDays < 7) {
                nbDays = 7
            }
        }

        int nbChilds = travel.travellers.findAll { t -> t == CHILD }.toList().size()
        int nbAdults = travel.travellers.findAll { t -> t == ADULT }.toList().size()
        int nbYoungs = travel.travellers.findAll { t -> t == YOUNG }.toList().size()
        int nbSeniors = travel.travellers.findAll { t -> t == SENIOR }.toList().size()


        if (!phase4On && phase3On) {
            //1) on ne paye la 2e semaine qu'à partir du 3e jours
            if (nbDays > 7 && nbDays < 10) {
                //nbDays=7
            }
            //2) au delà de 3 semaines,on ne facture que les semaines pleines
            if (((int) travel.nbDays / 7) >= 3) {
                //nbDays = ((int) travel.nbDays / 7) * 7
            }

            //12) Pack Jeune: au delà de 3 jeunes, 1 gratuit
            if (nbYoungs >= 3) {
                //passengersRisks-=ageRisk(YOUNG)
            }
        }

        double total = price * countryRisk * passengersRisks * nbDays + optionPrice
        if (phase4On) {
            double rp = romanPrice(nbDays)
            total = price * countryRisk * passengersRisks * rp + optionPrice
        }
        double totalTmp = total

        if (!phase4On && phase3On) {

            //3) réduction pack famille
            if (nbChilds >= 2 && nbAdults >= 2) {
                //totalTmp-=(total*20/100)
            }

            //11) malus trop de personnes agées
            if (nbSeniors > (nbAdults + nbChilds + nbYoungs)) {
                //totalTmp+=(total*nbSeniors/100)
            }
            //10) pas assez d'adultes encadrants
            if (nbChilds > nbAdults) {
                //totalTmp+=(total*15/100)
            }
            //13) au delà de 3 mois, on économise 5% par mois au delà
            if (nbDays / 30 > 3) {
                //totalTmp-=total*((nbDays/30)-3)*5/100
            }

            //8) 5% pour les couples seuls
            if (nbAdults == 2 && nbYoungs == 0 && nbChilds == 0 && nbSeniors == 0) {
                //totalTmp-=total*5/100
            }

            //5) 10% pour les jeunes couples
            if (nbAdults == 0 && nbYoungs == 2 && nbChilds == 0 && nbSeniors == 0) {
                //totalTmp-=total*10/100
            }
            //6) Pack Jeune++: au delà de 5 jeunes, 10% de promo
            if (nbYoungs >= 5) {
                //totalTmp-=total*10/100
            }
            //7) risque voyage solo: malus de 5%
            if (nbAdults + nbChilds + nbSeniors + nbYoungs == 1) {
                //totalTmp+=total*5/100
            }

        }
        totalTmp
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

    def Data generateData(Randomizator randomizator, Map config) {
        def availableCovers = config["coverPrices"].keySet() as Cover[]
        def availableCountries = config["countriesRisks"].keySet() as Country[]
        def availableOptions = config["optionsPrices"].keySet() as List<Option>

        Cover cover = randomizator.pickOne(availableCovers, { c -> c.rate() })
        Country country = randomizator.pickOne(availableCountries, { c -> c.populationInMillions() })
        LocalDate dpDate = LocalDate.now().plusDays(randomizator.randomInt(100))
        LocalDate reDate = dpDate.plusDays(randomizator.randomInt(150))
        int nbTraveller = randomizator.randomInt(5) + 1
        int[] ages = randomizator.randomPositiveInts(nbTraveller, 95)
        List<Option> options = availableOptions.findAll { o -> randomizator.randomDouble() < o.rate }.toList()

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

    @Override
    Question nextQuestion(int tick, Randomizator randomizator) {
        def config = carpaccio()
        Data data = generateData(randomizator, config)
        return new     QuestionInsuranceCrossSelling(data: data,travelData: toTravelData(data))
    }

    // -------------- TESTS --------------

    def testAll() {
        testSumOfRiskAdjustedAges()
        testStandardQuote()
        testDateCalculation()
    }

    def testSumOfRiskAdjustedAges() {
        def ageRisks = [
                (0) : 0.5,
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

    def testDateCalculation() {

        assert romanPrice(1) == 1
        assert romanPrice(2) == 2
        assert romanPrice(3) == 3
        assert romanPrice(4) == (4.4 - 1)
        assert romanPrice(5) == 4.4
        assert romanPrice(6) == 4.4 + 1
        assert romanPrice(7) == 4.4 + 2 * 1
        assert romanPrice(9) == (8.4 - 1)
        assert romanPrice(13) == (8.4 + 3 * 1)
        assert romanPrice(14) == 8.4 + (4.4 - 1)
        assert romanPrice(19) == (2 * 8.4 - 1)
        assert romanPrice(28) == (2 * 8.4 + 4.4 + 3 * 1)
        assert romanPrice(31) == (3 * 8.4 + 1)
        assert romanPrice(39) == (3 * 8.4 + 8.4 - 1)
        assert romanPrice(41) == (39 - 8.4) + 1
        assert romanPrice(42) == (39 - 8.4) + 2
        assert romanPrice(52) == 39 + 2
        assert romanPrice(77) == 39 + 2 * 8.4 + 4.4 + 2 * 1

        1.upto(88, {
            println "Number ${it} ${romanPrice(it)}"
        })
    }

    def testStandardQuote() {
        def config = phase2();
        println config

        1.upto(20, {
            def data = generateData(new Randomizator(), config)
            println data
            def quote = quote(data, config)
            println quote
        })
    }

}

generator = new QuestionInsuranceGenerator()

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
    CHILD, YOUNG, ADULT, SENIOR
}

public class QuestionInsuranceCrossSelling extends QuestionSupport implements Question {

    Data data
    TravelData travelData

    double penalty = -0d
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
            gains = reward
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
        def myReward = 0.0;

        // Abusing the cross-selling offers and you loose the customer trust completely

        if (offers.size > 4) {

            return -5.0
        }

        if (offers.any { offer -> offer.length() > 50}) {

            return -10.0
        }


        // Too many cross-selling offers and you sell none

        if (offers.size >= 3) {

            return 0

        }

        // Basic Cover is price-sensitive, only takes "free" offers

        if (data.cover == Cover.Basic) {

            if (offers.any { offer -> containsAny(offer, ["free"]) }) {
                myReward += 20
            }

        }


        if (data.cover == Cover.Extra) {

            if (offers.any { offer -> containsAny(offer, ["laser tag","laser","escape game","coding dojo", "coding game"]) }) {
                myReward += 20
            }

        }


        if ((data.travellers.contains(ADULT) || data.travellers.contains(SENIOR))  && offers.any { offer ->

            containsAny(offer, [

                    "restaurant",
                    "museum",
                    "guided",
                    "dinner"
            ])
        }) {

            myReward += 50

        }


        if ((data.travellers.contains(ADULT) || data.travellers.contains(SENIOR)) && data.travellers.length==2  && offers.any { offer ->

            containsAny(offer, [
                    "romantic"
            ])
        }) {

            myReward += 50

        }

        if (data.travellers.contains(YOUNG) && offers.any { offer ->

            containsAny(offer, [

                    "party",
                    "nightclub",
                    "car rental",
                    "rent car",
                    "boardgame",
                    "bar"
            ])
        }) {

            myReward += 50

        }

        // Departure in a a few days? You may need a visa / passport express

        if (data.nbDaysToDeparture < 21 && offers.any { offer ->

            containsAny(offer, ["visa", "passport"])
        }) {

            myReward += 50

        }

        // Senior or Cover.Premier enjoy luguage transfer and shuttle or valet

        if ((data.cover == Cover.Premier || data.travellers.contains(SENIOR))

                && offers.any { offer ->

            containsAny(offer, [

                    "luguage",

                    "baggage",

                    'transfer',

                    "shuttle"

            ])
        }) {

            myReward += 60

        }

        // Travelling with kids, may need a baby sitter or child care

        if (data.travellers.contains(CHILD) && offers.any { offer ->

            containsAny(offer, [

                    "baby sitter",

                    "baby sitting",

                    "babysitting",

                    "babysitter",

                    "child care",

                    "day care",

                    "child wellfare",

                    "nursery",

                    "trolley",

                    "stroller",

                    "baby-buggy",

                    "baby buggy",

                    "babybuggy"

            ])
        }) {

            myReward += 70

        }

        // Long term travel is like expat

        if (data.nbDays > 90 && offers.any { offer ->

            containsAny(offer, [

                    "moving",

                    "expat",

                    "relocation"

            ])
        }) {

            myReward += 40
        }

        // USA (MISSING COUNTRY) needs ESTA

        if (data.country == Country.FR && offers.any { offer ->

            containsAny(offer, ["futuroscope","disneyland"])
        }) {

            myReward += 60

        }

        if (data.country == Country.ES && offers.any { offer ->

            containsAny(offer, ["portaventura"])
        }) {

            myReward += 60

        }

        if (data.country == Country.DE && offers.any { offer ->

            containsAny(offer, ["europaparc","europa-parc","europa parc"])
        }) {

            myReward += 60

        }

        // USA (MISSING COUNTRY) is the country of cars

        if (offers.any { offer ->

            containsAny(offer, [

                    "car rental",

                    "car insurance"

            ])
        }) {

            myReward += 70

        }

        if (data.options.contains(Option.Skiing) && offers.any { offer ->

            containsAny(offer, [

                    "helmet",

                    "ski rental",

                    "rental ski",

                    "medical assistance",

                    "surf",

                    "snow tire",

                    "cap"

            ])
        }) {

            myReward += 40

        }

        if (data.options.contains(Option.Sports) && offers.any { offer ->

            containsAny(offer, [

                    "helmet",

                    "extreme",

                    "massage",

                    "sauna",

                    "hammam",

                    "energy",

                    "medical"
            ])
        }) {

            myReward += 40

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

            myReward += 60

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

            myReward += 50

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

            myReward += 55

        }

        return myReward

    }

    def containsAny(String token, List<String> keywords) {

        keywords.any { token.toLowerCase().contains(it) }

    }

}

//---------------TESTS---------------

def test_containsAny() {

    assert containsAny("mobile app", ["mobile", "ios"])

    assert containsAny("mobile app", ["app"])

    assert containsAny("expat magazine", ["expat"])

}

def testAll() {

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

    data.cover = Cover.Extra

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