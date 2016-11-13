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
       ...                 |    / Observable         | {RxNettyQuestionDispatcher.java}
      Player n <---------  |                         |
                           +-------------------------+

```

## Notes

* [x] Dashboard add code review ranking: Web Page; - Y.
* [ ] No random on ReductionMode; keep Standard on start, then eventually change it afterwards
* [ ] Feedback invocation / Question#accept => Question#diagnose to send a relevant feedback message
* [x] API Events - F.



* [ ] Bad requests: Generation/Activation 
* [ ] Configure logs: multiple log file: game/player/...
* [ ] Persist data
* [ ] 

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
