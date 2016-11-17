package fr.arolla.core.question;

import java.util.Random;

/**
 * Created by fmaury on 17/11/16.
 */
public class RandomBytesQuestion extends InvalidQuestionSupport{

    public static final Random RANDOM = new Random();
    private byte[] randomBytes;

    public RandomBytesQuestion(int sizeInBytes) {
        this.randomBytes = generate(sizeInBytes);
    }

    private byte[] generate(int sizeInBytes) {
        byte[] bytes = new byte[sizeInBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    @Override
    public Object questionData() {
        return randomBytes;
    }
}
