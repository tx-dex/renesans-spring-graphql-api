package fi.sangre.renesans.persistence.model;

import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.SurveyId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = {"organizationId", "surveyId"})
public class OrganizationSurveyMapping {
    private final OrganizationId organizationId;
    private final SurveyId surveyId;

    public OrganizationSurveyMapping(final UUID organizationId, final UUID surveyId) {
        this.organizationId = new OrganizationId(organizationId);
        this.surveyId = new SurveyId(surveyId);
    }
}
