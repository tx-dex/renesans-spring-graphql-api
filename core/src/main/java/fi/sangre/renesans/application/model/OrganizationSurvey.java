package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class OrganizationSurvey {
    private UUID id;
    private Long version;
    private ImageMetadata logo; //TODO: use different class not a persistence object
    private MultilingualText titles;
    private MultilingualText descriptions;
    private List<Catalyst> catalysts;
    private List<Parameter> parameters;
    private Map<String, StaticTextGroup> staticTexts;
    @Builder.Default
    private boolean deleted = false;
    //TODO: create OrganizationSurveyOutput and move this there
    private RespondentCounters respondentCounters;
}
