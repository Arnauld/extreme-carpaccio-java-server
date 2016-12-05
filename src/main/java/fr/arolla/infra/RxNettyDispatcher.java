package fr.arolla.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.arolla.core.*;
import fr.arolla.core.question.ResponseSupport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class RxNettyDispatcher implements QuestionDispatcher, FeedbackSender {

    public static final String QUOTE_PATH = "/quote";
    public static final String FEEDBACK_PATH = "/feedback";

    private final Logger log = LoggerFactory.getLogger(RxNettyDispatcher.class);
    private final Logger feedbacklog = LoggerFactory.getLogger(Feedback.class);

    private final ObjectMapper objectMapper;

    public RxNettyDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question question, Player player) {
        ByteBuf payload = toBytes(question);

        String playerUrl = player.url();
        if (playerUrl.endsWith("/")) {
            playerUrl = playerUrl.substring(0, playerUrl.length() - 1);
        }
        log.info("Tick {} - Invoking {} on {}", tick, player.username(), playerUrl);
        QuestionOfPlayer qop = new QuestionOfPlayer(question, player);

        URI uri;
        try {
            uri = new URI(playerUrl);
        } catch (URISyntaxException e) {
            log.warn("Fail to send question to player {}; url is invalid", player.username(), e);
            return Observable.just(qop.withStatus(QuestionOfPlayer.Status.Error));
        }

        HttpClientRequest<ByteBuf, ByteBuf> httpClientRequest = createHttpPostRequest(player.username(), uri, QUOTE_PATH);

        return httpClientRequest
                .writeContentAndFlushOnEach(Observable.just(payload))
                .flatMap(extractResponse(tick, player))
                .onErrorReturn(toError(tick, player))
                .timeout(10L, TimeUnit.SECONDS, Observable.just(ResponseDto.timeout()))
                .map(response -> consolidateResponse(qop, response));
    }

    private Func1<HttpClientResponse<ByteBuf>, Observable<? extends ResponseDto>> extractResponse(int tick, Player player) {
        return clientResponse -> {
            HttpResponseStatus status = clientResponse.getStatus();
            feedbacklog.info("Tick {} - response received from {}: {}", tick, player.username(), status);

            if (status.equals(HttpResponseStatus.OK)) {
                return toResponseObservable(tick, player, clientResponse);
            }
            if (status.equals(HttpResponseStatus.NO_CONTENT)) {
                return Observable.just(ResponseDto.skip());
            }
            if (status.equals(HttpResponseStatus.BAD_REQUEST)) {
                return Observable.just(ResponseDto.rejected());
            }
            return Observable.just(ResponseDto.invalid());

        };
    }

    private Observable<ResponseDto> toResponseObservable(int tick, Player player, HttpClientResponse<ByteBuf> clientResponse) {
        return clientResponse
                .getContent()
                .map(this::responseFromBytes)
                .map(r -> r.withStatus(QuestionOfPlayer.Status.OK))
                .onErrorReturn(toError(tick, player));
    }

    private Func1<Throwable, ResponseDto> toError(int tick, Player player) {
        return err -> {
            if (err instanceof ConnectException) {
                log.warn("Tick {} - Fail to connect to {}: {}", tick, player.username(), err.getMessage());
                return ResponseDto.offline();
            }
            log.error("Tick {} - Ooops for {}", tick, player.username(), err);
            return ResponseDto.error(err);
        };
    }

    @Override
    public void notify(int tick, Feedback feedback) {
        ByteBuf payload = toBytes(feedback);
        Player player = feedback.getPlayer();
        log.info("Notifying answer to player {} at tick {}", player.username(), tick);
        feedbacklog.info("sending feedback {} to player {} at tick {}", feedback, player.username(), tick);

        String playerUrl = player.url();
        if (playerUrl.endsWith("/")) {
            playerUrl = playerUrl.substring(0, playerUrl.length() - 1);
        }
        URI uri;
        try {
            uri = new URI(playerUrl);
        } catch (URISyntaxException e) {
            log.warn("Fail to notify player {}; url is invalid", player.username(), e);
            return;
        }

        HttpClientRequest<ByteBuf, ByteBuf> httpClientRequest = createHttpPostRequest(player.username(), uri, FEEDBACK_PATH);
        httpClientRequest
                .writeContentAndFlushOnEach(Observable.just(payload))
                .doOnError(
                        err -> log.error("Tick {} - error while sending feedback for {}", tick, player.username(), err)
                )
                .timeout(1L, TimeUnit.SECONDS)
                .subscribe();
    }

    private HttpClientRequest<ByteBuf, ByteBuf> createHttpPostRequest(String playerName, URI uri, String uripath) {
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }
        log.debug("contacting player {} on {} {} {}", playerName, host, port, uripath);
        return HttpClient.newClient(host, port)
                .followRedirects(3)
                .createPost(uripath)
                .addHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                .addHeader(HttpHeaderNames.ACCEPT, HttpHeaderValues.APPLICATION_JSON);
    }

    private QuestionOfPlayer consolidateResponse(QuestionOfPlayer qop, ResponseDto response) {
        log.info("Consolidating player {} response with {}", qop.getPlayer().username(), response);
        return qop.withStatus(response.status).withResponse(new ResponseSupport(response.response));
    }

    private ResponseDto responseFromBytes(ByteBuf byteBuf) {
        try {
            int len = byteBuf.readableBytes();
            byte[] bytes = new byte[len];
            byteBuf.readBytes(bytes);
            Map<String, Object> value = objectMapper.readValue(bytes, new TypeReference<Map<String, Object>>() {
            });
            log.debug("Data read from bytes: {}", value);
            return new ResponseDto().withResponse(value);
        } catch (Exception e) {
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
            log.error("Fail to serialize question {}", question, e);
            throw new RuntimeException("Fail to serialize question", e);
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

        public static ResponseDto skip() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.Skip);
        }

        public static ResponseDto offline() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.UnreachablePlayer);
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
