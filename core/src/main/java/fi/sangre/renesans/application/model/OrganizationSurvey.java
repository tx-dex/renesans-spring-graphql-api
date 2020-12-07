package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.parameter.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class OrganizationSurvey {
    private UUID id;
    private Long version;
    private MultilingualText titles;
    private MultilingualText descriptions;
    private List<Catalyst> catalysts;
    private List<Parameter> parameters;
    private List<StaticTextGroup> staticTexts;
    @Builder.Default
    private boolean deleted = false;
}
