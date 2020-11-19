package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    private List<CatalystMetadata> catalysts;
    private List<ParameterMetadata> parameters;
    private LocalisationMetadata localisation;
}
