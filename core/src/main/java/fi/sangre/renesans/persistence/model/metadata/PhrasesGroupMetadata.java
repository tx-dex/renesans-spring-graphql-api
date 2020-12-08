package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@ToString

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhrasesGroupMetadata implements Serializable {
    private final Map<String, PhrasesMetadata> phrases;

    public PhrasesGroupMetadata() {
        phrases = new LinkedHashMap<>();
    }

    public PhrasesGroupMetadata(@NonNull final Map<String, PhrasesMetadata> phrases) {
        this.phrases = new LinkedHashMap<>(phrases);
    }

    @JsonAnyGetter
    public Map<String, PhrasesMetadata> getPhrases() {
        return phrases;
    }

    @JsonAnySetter
    public void setPhrase(@NonNull final String key, @NonNull final PhrasesMetadata value) {
        phrases.put(key, value);
    }
}
