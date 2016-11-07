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
      Player 1 &lt;-----------  |                         |
      Player 2 &lt;-----------  |  RxNetty Clients        |
       ...                    |    / Observable         |
      Player n &lt;-----------  |                         |
                              +-------------------------+

```