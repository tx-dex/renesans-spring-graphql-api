package fi.sangre.renesans.application.model.filter;

import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class RespondentParameterFilter implements RespondentFilter {
    private UUID id;
    private List<UUID> values;
}
