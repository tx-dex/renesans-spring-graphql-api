package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.SurveyCounters;
import fi.sangre.renesans.graphql.output.aaa.UserOutput;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class OrganizationOutput {
    private UUID id;
    private String name;
    private String description;
    private RespondentCounters respondentCounters;
    private SurveyCounters surveyCounters;
    private UserOutput owner;
}
