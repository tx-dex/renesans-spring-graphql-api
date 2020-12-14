package fi.sangre.renesans.persistence.model.metadata.parameters;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ParameterItemMetadata implements ParameterChildMetadata {
    private UUID id;
    private Map<String,String> titles;
}
