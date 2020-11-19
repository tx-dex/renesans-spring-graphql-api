package fi.sangre.renesans.persistence.model.metadata.parameters;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.api.client.util.Lists;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ListParameterMetadata implements ParameterMetadata {
    private UUID id;
    private Map<String,String> titles;
    @Builder.Default
    private List<ParameterItemMetadata> values = Lists.newArrayList();
}
