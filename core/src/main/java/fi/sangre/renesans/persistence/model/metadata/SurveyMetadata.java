package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.persistence.model.metadata.discussion.DiscussionQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.MediaMetadata;
import fi.sangre.renesans.persistence.model.metadata.parameters.ParameterMetadata;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyMetadata implements Serializable {
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private ImageMetadata logo;
    private List<MediaMetadata> media;
    private List<CatalystMetadata> catalysts;
    private Boolean hideCatalystThemePages;
    private List<ParameterMetadata> parameters;
    private List<DiscussionQuestionMetadata> discussionQuestions;
    private LocalisationMetadata localisation;
    private Map<String, PhrasesGroupMetadata> translations;
}
