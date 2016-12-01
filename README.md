[extreme-carpaccio](https://github.com/dlresende/extreme-carpaccio) Java based server implementation


# Developer's notes

* [awaitility](https://github.com/awaitility/awaitility/wiki/Usage)
* [Spring boot test](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* [RxNetty](https://github.com/ReactiveX/RxNetty)

```
$ mvn clean install
```

Overrides configuration with VM argument.

```
-DquestionGenerator.scriptPaths=/Users/Arnauld/Projects/extreme-carpaccio-springboot/script/questions.groovy,\
                                /Users/Arnauld/Projects/extreme-carpaccio-springboot/script/priceWithTax.groovy,\
                                /Users/Arnauld/Projects/extreme-carpaccio-springboot/script/insurance.groovy
```

## Architecture

```
                           +-------------------------+
Dashboard ---------------> |                         |
                           |  SpringBoot Web         | {WebController.java} ...> {Players.java}
Player registration -----> |                         |                            ^
                           |                         |                           /
                           | ----------------------- |                       ...' 
                           |                         | {Game.java} .........'
      Player 1 <---------  |                         |        :             `....> {QuestionGenerator.java}
      Player 2 <---------  |  RxNetty Clients        |        v
       ...                 |    / Observable         | {RxNettyDispatcher.java}
      Player n <---------  |                         |

                           +-------------------------+

```

## Usage

* thecodinggame.groovy, invalid-insurance.groovy, insurance-iteration-1.groovy, technical-invalid-requests.groovy

STEP 1 :
with each weight at 0.0 except insurance-iteration-1 at 1.0

line 120 --> change data (uncomment)
RULES = cover (basic 1.8) * nb travellers * duration days (return date - departure date)

STEP 2 : weight at 0.0 for all except thecodinggame at 1.0

goal : let the team slices
RULES = cover * sum risk travellers * min (7, duration days) * country + options

STEP 3 : in thecodinggame.quote(), set phase3On to True, then play with the multiple if

RULES = ANNOUNCE changes, and uncomment/add if cases
BONUS : start to send invalid quotes, by modifying the weight of invalid-insurance and codingame

STEP 4 :

STEP 5 : replace the nextQuestion with the commented one REPLACE FOR IT5

RULES : find offers to send instead of quote, offers format is an array like {"offers":["off1","off2"]}

## Protocol

### Price with tax

```javascript
{
  "quantities" : [1, 3, 6, 2],
  "prices" : [1.2, 3.6, 7.5, 8.2],
  "reduction": "Standard|HalfPrice|PayThePrice",
  "country": "NL"
}
```

```javascript
{
  "total":12.3
}
```

### Question

```javascript
{
  "question":"Which word in the list designate a programming language?",
  "choices":["jawa","jaba","java"]
}
```

```javascript
{
  "response":"java"
}
```

```javascript
{
  "question":"What is 4 plus 5?"
}
```

```javascript
{
  "response":"9"
}
```

### Insurrance

```javascript
{
  "country":"PL",
  "departureDate":"2016-11-15",
  "returnDate":"2016-12-09",
  "travellerAges":[32,39],
  "options":["SportsAndActivities","MedicalConditions"],
  "cover":"Premier"
}
```

```javascript
{
  "quote": 234.5,
  "options":["SportsAndActivities","MedicalConditions"]
}
```
# Inspirations

* https://diegolemos.net/2016/01/07/extreme-carpaccio/
* [extreme-carpaccio - github](https://github.com/dlresende/extreme-carpaccio)
* http://chatley.com/posts/05-27-2011/extreme-startup/
* [rchatley - github](https://github.com/rchatley/extreme_startup/blob/master/lib/extreme_startup/question_factory.rb)
