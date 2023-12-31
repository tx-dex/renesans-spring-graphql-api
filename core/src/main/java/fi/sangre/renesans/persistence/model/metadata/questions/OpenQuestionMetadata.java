package fi.sangre.renesans.persistence.model.metadata.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.persistence.model.metadata.references.MetadataReference;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenQuestionMetadata implements QuestionMetadata {
    private UUID id;
    private Map<String,String> titles;
    private MetadataReference reference;
}
