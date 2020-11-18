package fi.sangre.renesans.persistence.model.metadata.parameters;

import com.google.api.client.util.Lists;
import fi.sangre.renesans.persistence.model.metadata.MultilingualMetadata;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class ListParameterMetadata implements ParameterMetadata {
    private UUID id;
    private MultilingualMetadata label;
    @Builder.Default
    private List<ParameterItemMetadata> values = Lists.newArrayList();
}
