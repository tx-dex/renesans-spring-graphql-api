package fi.sangre.renesans.persistence.model.metadata.parameters;

import fi.sangre.renesans.persistence.model.metadata.MultilingualMetadata;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class TreeParameterMetadata implements ParameterMetadata, ParameterChildMetadata {
    private UUID id;
    private MultilingualMetadata label;
    private List<ParameterChildMetadata> children;
}
