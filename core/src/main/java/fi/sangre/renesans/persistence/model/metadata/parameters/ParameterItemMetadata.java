package fi.sangre.renesans.persistence.model.metadata.parameters;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class ParameterItemMetadata implements ParameterChildMetadata {
    private UUID id;
    private Map<String,String> titles;
}
