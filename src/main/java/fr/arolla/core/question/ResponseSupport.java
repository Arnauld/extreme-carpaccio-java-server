package fr.arolla.core.question;

import fr.arolla.core.Question;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ResponseSupport implements Question.Response {

    private final Map<String, Object> values;

    public ResponseSupport(String key, Object value) {
        this(singletonMap(key, value));
    }

    public ResponseSupport(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        if(values==null){
            return Optional.empty();
        }
        Object o = values.get(key);
        if (o != null && type.isInstance(o))
            return Optional.of(type.cast(o));

        return Optional.empty();
    }
}
