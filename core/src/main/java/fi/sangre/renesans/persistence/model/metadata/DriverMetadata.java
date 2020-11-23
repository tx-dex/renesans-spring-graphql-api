package fi.sangre.renesans.persistence.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.graphql.output.DriverOutput;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverMetadata implements Serializable, DriverOutput {
    private Long id;
    @Deprecated
    private String pdfName;
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private Map<String, String> prescriptions;
    @Builder.Default
    private Double weight = 0.5;
}
