package fi.sangre.renesans.persistence.model.metadata.parameters;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class TreeParameterMetadata implements ParameterMetadata, ParameterChildMetadata {
    private UUID id;
    private Map<String,String> titles;
    private List<ParameterChildMetadata> children;
}
