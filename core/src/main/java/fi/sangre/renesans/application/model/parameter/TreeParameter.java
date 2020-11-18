package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class TreeParameter implements Parameter, ParentParameter, ParameterChild {
    private UUID id;
    private MultilingualText label;
    private List<ParameterChild> children;
}
