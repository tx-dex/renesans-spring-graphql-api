package fi.sangre.renesans.application.model;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MultilingualText {
    private final Map<String, String> phrases;

    public MultilingualText(final Map<String, String> phrases) {
        //TODO: should never set null
        this.phrases = phrases;
    }

    public boolean isEmpty() {
        return phrases == null || phrases.isEmpty();
    }

    public static MultilingualText EMPTY = new MultilingualText(ImmutableMap.of());
}
