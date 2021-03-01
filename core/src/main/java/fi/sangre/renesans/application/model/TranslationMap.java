package fi.sangre.renesans.application.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Map;

@NoArgsConstructor
@Data

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TranslationMap {
    private final Map<String, Map<String, TranslationText>> translations = Maps.newHashMap();

    @JsonAnySetter
    public void setTranslations(@NonNull final String group, @NonNull final Map<String, TranslationText> value) {
        this.translations.put(group, value);
    }

    @NonNull
    public Map<String, Map<String, TranslationText>>  getTranslations() {
        return translations;
    }
}
