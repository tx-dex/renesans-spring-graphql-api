package fi.sangre.renesans.application.model;

import com.google.common.collect.ImmutableMap;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Objects;

@ToString
public class MultilingualText {
    private final Map<String, String> phrases;

    public MultilingualText(final Map<String, String> phrases) {
        //TODO: should never set null
        this.phrases = phrases;
    }

    public boolean isEmpty() {
        return phrases == null
                || phrases.values().stream().allMatch(Objects::isNull);
    }

    public Map<String, String> getPhrases() {
        if (phrases == null) {
            return ImmutableMap.of();
        } else {
            return phrases;
        }
    }

    public String getPhrase(@NonNull final String languageTag) {
        if (phrases == null) {
            return null;
        } else {
            return phrases.get(languageTag);
        }
    }
}
