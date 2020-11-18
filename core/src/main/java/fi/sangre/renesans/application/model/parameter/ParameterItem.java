package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class ParameterItem implements ParameterChild {
    private UUID id;
    private MultilingualText label;
}
