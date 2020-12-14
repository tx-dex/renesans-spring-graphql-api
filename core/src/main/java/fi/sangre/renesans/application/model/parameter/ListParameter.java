package fi.sangre.renesans.application.model.parameter;

import com.google.api.client.util.Lists;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class ListParameter implements Parameter {
    private ParameterId id;
    private MultilingualText label;
    @Builder.Default
    private List<ParameterItem> values = Lists.newArrayList();
}
