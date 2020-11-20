package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
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
    private List<Parameter> parameters;
    private List<StaticText> staticTexts;
    private SurveyMetadata metadata;
    @Builder.Default
    private boolean deleted = false;
}
