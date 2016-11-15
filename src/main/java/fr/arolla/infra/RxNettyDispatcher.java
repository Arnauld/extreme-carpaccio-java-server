package fr.arolla.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.*;
import fr.arolla.core.question.ResponseSupport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class RxNettyDispatcher implements QuestionDispatcher, FeedbackSender {

    private final Logger log = LoggerFactory.getLogger(RxNettyDispatcher.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public RxNettyDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question question, Player player) {
        ByteBuf payload = toBytes(question);

        log.info("Tick {} - Invoking {} on {}", tick, player.username(), player.url());
        QuestionOfPlayer qop = new QuestionOfPlayer(question, player);

        return RxNetty.createHttpPost(player.url(), Observable.just(payload))
                .flatMap(clientResponse -> {
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
                        return Observable.just(ResponseDto.rejected());
                    }
                    return Observable.just(ResponseDto.invalid());

                })
                .onErrorReturn(err -> {
                    log.error("Tick {} - Ooops for {}", tick, player.username(), err);
                    return ResponseDto.error(err);
                })
                .timeout(10L, TimeUnit.SECONDS, Observable.just(ResponseDto.timeout()))
                .map(response -> consolidateResponse(qop, response));
    }

    @Override
    public void notify(Feedback feedback, int tick) {
        ByteBuf payload = toBytes(feedback);
        Player player = feedback.getPlayer();
        log.info("Notifying answer to player {} at tick {}", player.username(), tick);
        log.debug("sending feedback {} to player {} at tick {}", feedback, player.username(), tick);
        sendFeedback(player, tick, payload);
    }

    private void sendFeedback(Player player, int tick, ByteBuf payload) {
        RxNetty.createHttpPost(player.url(), Observable.just(payload))
                .doOnError(
                        err -> log.error("Tick {} - error while sending feedback for {}", tick, player.username(), err)
                )
                .timeout(10L, TimeUnit.SECONDS);
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

    private ByteBuf toBytes(Question question) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(question.questionData());
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buffer.writeBytes(bytes);
            return buffer;
        } catch (JsonProcessingException e) {
            log.error("Fail to serialize order {}", question, e);
            throw new RuntimeException("Fail to serialize order", e);
        }
    }

    private ByteBuf toBytes(Feedback feedback) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(feedback.data());
            ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buffer.writeBytes(bytes);
            return buffer;
        } catch (JsonProcessingException e) {
            log.error("Fail to serialize order {}", feedback, e);
            throw new RuntimeException("Fail to serialize order", e);
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
