package fi.sangre.renesans.application.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class SurveyTemplate {
    private TemplateId id;
    private Long version;
    private MultilingualText titles;
    private MultilingualText descriptions;
}
