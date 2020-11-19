package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class LocalisationMetadata implements Serializable {
    private static final String DEFAULT_LANGUAGE = "fi";
    private static final List<String> DEFAULT_LANGUAGES = ImmutableList.of("fi", "en");

    @Builder.Default
    private String defaultLanguage = DEFAULT_LANGUAGE;
    @Builder.Default
    private List<String> languages = Lists.newArrayList(DEFAULT_LANGUAGES);
}
