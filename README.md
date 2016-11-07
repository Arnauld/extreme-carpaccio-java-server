[extreme-carpaccio](https://github.com/dlresende/extreme-carpaccio) Java based server implementation


# Developer's notes

* [awaitility](https://github.com/awaitility/awaitility/wiki/Usage)
* [Spring boot test](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* [RxNetty](https://github.com/ReactiveX/RxNetty)

```
$ mvn clean install
```

```
                              +-------------------------+
Dashboard ------------------> |                         |
                              |  SpringBoot Web         |
Player registration --------> |                         |
                              |                         |
                              | ----------------------- |
                              |                         |
      Player 1 <------------  |                         |
      Player 2 <------------  |  RxNetty Clients        |
       ...                    |    / Observable         |
      Player n <------------  |                         |
                              +-------------------------+

```
