package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalystMetadata implements Serializable {
    private UUID id;
    @Deprecated
    private String pdfName;
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private List<DriverMetadata> drivers;
    private List<QuestionMetadata> questions;
    private Map<String, String> openQuestion;
    @Builder.Default
    private Double weight = 0.5;
}
