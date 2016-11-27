package fr.arolla.infra;

import fr.arolla.core.Player;
import fr.arolla.core.Question;
import fr.arolla.core.QuestionDispatcher;
import fr.arolla.core.QuestionOfPlayer;
import fr.arolla.core.question.CorruptedQuestion;
import fr.arolla.core.question.ResponseSupport;
import fr.arolla.util.Randomizator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RxNettyCorruptedDispatcher implements QuestionDispatcher {

    private final Logger log = LoggerFactory.getLogger(RxNettyCorruptedDispatcher.class);

    private final Randomizator randomizator;

    public RxNettyCorruptedDispatcher(Randomizator randomizator) {
        this.randomizator = randomizator;
    }

    @Override
    public Observable<QuestionOfPlayer> dispatchQuestion(int tick, Question question, Player player) {
        log.info("Tick {} - Invoking {} on {} with corrupted question", tick, player.username(), player.url());
        QuestionOfPlayer qop = new QuestionOfPlayer(question, player);

        URI uri;
        try {
            uri = new URI(player.url());
        } catch (URISyntaxException e) {
            return Observable.just(qop.withStatus(QuestionOfPlayer.Status.Error));
        }

        CorruptedQuestion cq = (CorruptedQuestion) question;

        Observable<ByteBuf> payload = payloadOf(cq);
        return defineHeaders(HttpClient.newClient(uri.getHost(), uri.getPort())
                .followRedirects(3)
                .createPost(uri.getPath()), cq)
                .writeContentAndFlushOnEach(payload)
                .map(clientResponse -> {
                    HttpResponseStatus status = clientResponse.getStatus();
                    log.info("Tick {} - response received from {}: {}", tick, player.username(), status);

                    if (status.equals(HttpResponseStatus.OK) || status.equals(HttpResponseStatus.BAD_REQUEST)) {
                        return ResponseDto.ok();
                    }
                    return ResponseDto.invalid();
                })
                .onErrorReturn(err -> {
                    if (cq.accepts(err)) {
                        return ResponseDto.ok();
                    }
                    log.error("Tick {} - Ooops for {}", tick, player.username(), err);
                    return ResponseDto.error(err);
                })
                .timeout(60L, TimeUnit.SECONDS, Observable.just(ResponseDto.timeout()))
                .map(response -> consolidateResponse(qop, response));
    }

    private HttpClientRequest<ByteBuf, ByteBuf> defineHeaders(HttpClientRequest<ByteBuf, ByteBuf> request, CorruptedQuestion cq) {
        if (!cq.playWithHeaders()) {
            return request;
        }

        HttpClientRequest<ByteBuf, ByteBuf> req = request;
        for (CorruptedQuestion.Header header : cq.headersToPlayWith()) {
            switch (header) {
                case ContentLength:
                    req = req.addHeader(HttpHeaderNames.CONTENT_LENGTH, 1_000_000_000);
                    break;
                case ContentType:
                    req = req.addHeader(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=u-3615");
                    break;
                case Accept:
                    req = req.addHeader(HttpHeaderNames.ACCEPT, "application/jsooooooon; charset=u-3615");
                    break;
                case Random:
                    for (int i = 0; i < 500; i++) {
                        req = req.addHeader("X-header-" + i, "application/json; charset=u-3" + i);
                    }
                    break;
            }
        }
        return req;
    }

    private Observable<ByteBuf> payloadOf(CorruptedQuestion cq) {
        if (cq.useRandomBytes()) {
            return Observable.create(new Observable.OnSubscribe<ByteBuf>() {
                @Override
                public void call(final Subscriber<? super ByteBuf> subscriber) {
                    AtomicLong bytesWritten = new AtomicLong();
                    new Thread(() -> {
                        while (!subscriber.isUnsubscribed()) {
                            int nb = 1024 * 1024;
                            bytesWritten.addAndGet(nb);
                            ByteBuf bytes = randomBytes(nb, randomizator);
                            subscriber.onNext(bytes);

                            if (cq.minBytesToWrite() < bytesWritten.get() && cq.closeUnexpectedly()) {
                                subscriber.onError(new RuntimeException("EOF..."));
                                return;
                            }
                            if (cq.maxBytesToWrite() <= bytesWritten.get()) {
                                subscriber.onCompleted();
                                return;
                            }
                        }
                    }).start();
                }
            });
        }
        return Observable.from(cq.bytes()).map(RxNettyCorruptedDispatcher::toByteBuff);
    }

    private static ByteBuf toByteBuff(byte[] bytes) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(bytes.length);
        buffer.writeBytes(bytes);
        return buffer;
    }

    private static ByteBuf randomBytes(int sz, Randomizator randomizator) {
        byte[] randomBytes = new byte[sz];
        randomizator.randomBytes(randomBytes);
        return toByteBuff(randomBytes);
    }

    private QuestionOfPlayer consolidateResponse(QuestionOfPlayer qop, ResponseDto response) {
        log.info("Consolidating response with {}", response);
        return qop.withStatus(response.status).withResponse(new ResponseSupport(response.response));
    }

    public static class ResponseDto {
        public Map<String, Object> response;

        public Throwable error;

        public QuestionOfPlayer.Status status;

        public ResponseDto() {
        }

        public static ResponseDto ok() {
            return new ResponseDto().withStatus(QuestionOfPlayer.Status.OK);
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
