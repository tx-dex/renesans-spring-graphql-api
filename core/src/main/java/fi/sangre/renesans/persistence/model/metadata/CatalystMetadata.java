package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.graphql.output.CatalystOutput;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "pdfName")
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalystMetadata implements Serializable, CatalystOutput {
    private Long id;
    private String pdfName;
    private Map<String, String> titles;
    private List<DriverMetadata> drivers;
    private List<QuestionMetadata> questions;
    @Builder.Default
    private Double weight = 0.5;
}
