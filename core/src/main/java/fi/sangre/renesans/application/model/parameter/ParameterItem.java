package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"root", "parent"})
@Builder
public class ParameterItem implements ParameterChild {
    private Parameter root;
    private Parameter parent;
    private ParameterId id;
    private MultilingualText label;
}
