package fi.sangre.renesans.persistence.model.metadata.parameters;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TreeParameterMetadata implements ParameterMetadata, ParameterChildMetadata {
    private UUID id;
    private Map<String,String> titles;
    private List<ParameterChildMetadata> children;
}
