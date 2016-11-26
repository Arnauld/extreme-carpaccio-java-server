package fr.arolla.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.*;
import fr.arolla.core.question.ResponseSupport;
import fr.arolla.core.question.invalid.CorruptedQuestion;
import fr.arolla.core.question.invalid.EOFQuestion;
import fr.arolla.core.question.invalid.RandomBytesQuestion;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static rx.Observable.just;
import static rx.Observable.range;

public class RxNettyCorruptionDispatcher implements QuestionDispatcher, FeedbackSender {

    public static final Random RANDOM = new Random();
    private final Logger log = LoggerFactory.getLogger(RxNettyCorruptionDispatcher.class);

    private final ObjectMapper objectMapper;
    private Random random = new Random();

    @Autowired
    public RxNettyCorruptionDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question question, Player player) {

        log.info("Tick {} - Invoking {} on {}", tick, player.username(), player.url());
        QuestionOfPlayer qop = new QuestionOfPlayer(question, player);

        Observable<HttpClientResponse<ByteBuf>> httpPost = createHttpPost(player, question);

        return httpPost.flatMap(clientResponse -> {
            HttpResponseStatus status = clientResponse.getStatus();
            log.info("Tick {} - response received from {}: {}", tick, player.username(), status);

            if (status.equals(HttpResponseStatus.OK)) {
                return clientResponse
                        .getContent()
                        .map(this::fromBytes)
                        .map(r -> r.withStatus(QuestionOfPlayer.Status.OK))
                        .onErrorReturn(err -> {
                            log.error("Tick {} - Ooops for {}", tick, player.username(), err);
                            return ResponseDto.error(err);
                        });
            }
            if (status.equals(HttpResponseStatus.BAD_REQUEST)) {
                return just(ResponseDto.rejected());
            }
            return just(ResponseDto.invalid());

        })
                .onErrorReturn(err -> {
                    log.error("Tick {} - Ooops for {}", tick, player.username(), err);
                    return ResponseDto.error(err);
                })
                .timeout(10L, TimeUnit.SECONDS, just(ResponseDto.timeout()))
                .map(response -> consolidateResponse(qop, response));
    }

    private Observable<HttpClientResponse<ByteBuf>> createHttpPost(Player player, Question question) {
        String url = player.url() + "/quote";
        ByteBuf value = toBytes("{\"country\":\"PL\",\"departureDate\":\"2016-12-02\", \"returnDate\":\"2016-12-25\",\"travellerAges\":[82],\"options\":[],\"cover\":\"Basic\"}");
        if (question instanceof RandomBytesQuestion) {
            return RxNetty.createHttpPost(url, range(0, 10000).map(this::randomByteBuff));
        }

        if (question instanceof EOFQuestion) {

            return RxNetty.createHttpRequest(
                    HttpClientRequest.createPost(url)
                            .withContentSource(Observable.just(value.readBytes(random.nextInt(100))))
                                    .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                                    .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json"));
        }
        if (question instanceof CorruptedQuestion) {
            value.setByte(8, 4);
            value.setByte(16, 4);
            value.setByte(25, 4);

            return RxNetty.createHttpRequest(
                    HttpClientRequest.createPost(url)
                            .withContentSource(Observable.just(value))
                            .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                            .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json")
                            .withHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), "999999")
            );

        }
        return RxNetty.createHttpRequest(
                HttpClientRequest.createPost(url)
                        .withContentSource(Observable.just(value.readBytes(random.nextInt(50))))
                        .withHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
                        .withHeader(HttpHeaderNames.ACCEPT.toString(), "application/json")
                        .withHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), "-5")
        );
    }

    private ByteBuf randomByteBuff(Integer x) {
        byte[] randomBytes = new byte[5];
        RANDOM.nextBytes(randomBytes);
        ByteBuf payload = ByteBufAllocator.DEFAULT.buffer(randomBytes.length);
        payload.writeBytes(randomBytes);
        return payload;
    }

    @Override
    public void notify(Feedback feedback, int tick) {
        ByteBuf payload = toBytes(feedback);
        Player player = feedback.getPlayer();
        log.info("Notifying answer to player {} at tick {}", player.username(), tick);
        log.debug("sending feedback {} to player {} at tick {}", feedback, player.username(), tick);
        RxNetty.createHttpPost(player.url() + "/feedback", just(payload))
                .doOnError(
                        err -> log.error("Tick {} - error while sending feedback for {}", tick, player.username(), err)
                )
                .timeout(10L, TimeUnit.SECONDS)
                .subscribe();
    }

    private QuestionOfPlayer consolidateResponse(QuestionOfPlayer qop, ResponseDto response) {
        log.info("Consolidating response with {}", response);
        return qop.withStatus(response.status).withResponse(new ResponseSupport(response.response));
    }

    private ResponseDto fromBytes(ByteBuf byteBuf) {
        try {
            int len = byteBuf.readableBytes();
            byte[] bytes = new byte[len];
            byteBuf.readBytes(bytes);
            Map<String, Object> value = objectMapper.readValue(bytes, new TypeReference<Map<String, Object>>() {
            });
            log.debug("Data read from bytes: {}", value);
            return new ResponseDto().withResponse(value);
        } catch (IOException e) {
            log.error("Ooops while reading OrderResponseDto", e);
            return ResponseDto.error(e);
        }
    }

    private ByteBuf toBytes(Feedback feedback) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(feedback.data());
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buffer.writeBytes(bytes);
            return buffer;
        } catch (JsonProcessingException e) {
            log.error("Fail to serialize feedback {}", feedback, e);
            throw new RuntimeException("Fail to serialize feedback", e);
        }
    }

    private ByteBuf toBytes(String data) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(data);
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buffer.writeBytes(bytes);
            return buffer;
        } catch (JsonProcessingException e) {
            log.error("Fail to serialize question {}", data, e);
            throw new RuntimeException("Fail to serialize question", e);
        }
    }

    public static class ResponseDto {
        public Map<String, Object> response;

        public Throwable error;

        public QuestionOfPlayer.Status status;

        public ResponseDto() {
        }

        public static ResponseDto error(Throwable err) {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.Error).withError(err);
        }

        public static ResponseDto invalid() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.InvalidResponse);
        }

        public static ResponseDto rejected() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.QuestionRejected);
        }


        public static ResponseDto timeout() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.Timeout);
        }

        private ResponseDto withResponse(Map<String, Object> response) {
            this.response = response;
            return this;
        }

        private ResponseDto withError(Throwable error) {
            this.error = error;
            return this;
        }

        private ResponseDto withStatus(QuestionOfPlayer.Status status) {
            this.status = status;
            return this;
        }

        @Override
        public String toString() {
            return "ResponseDto{" +
                    ", response='" + response + '\'' +
                    ", error=" + error +
                    ", status=" + status +
                    '}';
        }

    }

}
