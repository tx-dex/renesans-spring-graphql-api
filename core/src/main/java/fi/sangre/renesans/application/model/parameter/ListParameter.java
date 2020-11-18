package fi.sangre.renesans.application.model.parameter;

import com.google.api.client.util.Lists;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class ListParameter implements Parameter {
    private UUID id;
    private MultilingualText label;
    @Builder.Default
    private List<ParameterItem> values = Lists.newArrayList();
}
