package fr.arolla.core.question;

import javax.validation.constraints.NotNull;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class CorruptedQuestion extends QuestionSupport {
    public enum Header {
        ContentLength,
        ContentType,
        Random, Accept,
    }

    private Header[] headers;
    private long minBytesToWrite = 1024 * 1024 * 8L;
    private long maxBytesToWrite = 1024 * 1024 * 16L;
    private boolean closeUnexpectedly;
    private Iterable<byte[]> bytes;

    public CorruptedQuestion() {
    }

    @Override
    public Object questionData() {
        return null;
    }

    @Override
    public ResponseValidation accepts(@NotNull Response response) {
        return ResponseValidation.of(true, () -> "No response expected");
    }

    public boolean accepts(Throwable err) {
        return true;
    }

    public boolean useRandomBytes() {
        return bytes == null;
    }

    public Iterable<byte[]> bytes() {
        return bytes;
    }

    public CorruptedQuestion bytes(Iterable<byte[]> bytes) {
        this.bytes = bytes;
        return this;
    }

    public CorruptedQuestion minBytesToWrite(long minBytesToWrite) {
        this.minBytesToWrite = minBytesToWrite;
        return this;
    }

    public long minBytesToWrite() {
        return minBytesToWrite;
    }

    public CorruptedQuestion maxBytesToWrite(long maxBytesToWrite) {
        this.maxBytesToWrite = maxBytesToWrite;
        return this;
    }

    public long maxBytesToWrite() {
        return maxBytesToWrite;
    }

    public boolean closeUnexpectedly() {
        return closeUnexpectedly;
    }

    public CorruptedQuestion closeUnexpectedly(boolean closeUnexpectedly) {
        this.closeUnexpectedly = closeUnexpectedly;
        return this;
    }

    public boolean playWithHeaders() {
        return headers != null && headers.length > 0;
    }

    public CorruptedQuestion headersToPlayWith(Header... headers) {
        this.headers = headers;
        return this;
    }

    public Header[] headersToPlayWith() {
        return headers;
    }
}
