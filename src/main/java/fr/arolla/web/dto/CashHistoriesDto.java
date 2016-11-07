package fr.arolla.web.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class CashHistoriesDto {

    public final int lastIteration;
    public final Map<String, double[]> history = new HashMap<>();

    public CashHistoriesDto(int lastIteration) {
        this.lastIteration = lastIteration;
    }

    public CashHistoriesDto append(String username, double[] cashHistory) {
        history.put(username, cashHistory);
        return this;
    }

    public CashHistoriesDto mergeWith(CashHistoriesDto other) {
        history.putAll(other.history);
        return this;
    }
}
