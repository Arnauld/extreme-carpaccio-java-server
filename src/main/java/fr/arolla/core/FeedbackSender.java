package fr.arolla.core;

/**
 * Created by Domo-kun on 14/11/2016.
 */
public interface FeedbackSender {
    void notify(int tick, Feedback feedback);

}
