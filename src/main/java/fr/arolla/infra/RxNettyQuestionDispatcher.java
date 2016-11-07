package fr.arolla.infra;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.Player;
import fr.arolla.core.Question;
import fr.arolla.core.QuestionDispatcher;
import fr.arolla.core.QuestionOfPlayer;
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
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@Service
public class RxNettyQuestionDispatcher implements QuestionDispatcher {

    private final Logger log = LoggerFactory.getLogger(RxNettyQuestionDispatcher.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public RxNettyQuestionDispatcher(ObjectMapper objectMapper) {
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

    private QuestionOfPlayer consolidateResponse(QuestionOfPlayer qop, ResponseDto response) {
        log.info("Consolidating response with {}", response);
        return qop.withStatus(response.status);
    }

    private ResponseDto fromBytes(ByteBuf byteBuf) {
        try {
            int len = byteBuf.readableBytes();
            byte[] bytes = new byte[len];
            byteBuf.readBytes(bytes);
            return objectMapper.readValue(bytes, ResponseDto.class);
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

    public static class ResponseDto {
        @JsonProperty
        public Double total;

        @JsonProperty
        public String response;

        // Transient property - ease Observable chaining
        @JsonIgnore
        public Throwable error;

        // Transient property - ease Observable chaining
        @JsonIgnore
        public QuestionOfPlayer.Status status;

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
                    "total=" + total +
                    ", response='" + response + '\'' +
                    ", error=" + error +
                    ", status=" + status +
                    '}';
        }

    }

}
