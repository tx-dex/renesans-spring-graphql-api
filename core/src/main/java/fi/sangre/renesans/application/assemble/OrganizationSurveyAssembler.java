package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.persistence.model.Survey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class OrganizationSurveyAssembler {

    @NonNull
    public OrganizationSurvey from(@NonNull final Survey survey) {
        return OrganizationSurvey.builder()
                .id(survey.getId())
                .version(survey.getVersion())
                .metadata(survey.getMetadata())
                .build();
    }
}
