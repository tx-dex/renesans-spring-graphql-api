package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireAssembler {
    private final OrganizationSurveyService organizationSurveyService;

    @NonNull
    public QuestionnaireOutput from(@NonNull final OrganizationSurvey survey) {
        return QuestionnaireOutput.builder()
                .id(survey.getId())
                .catalysts(ImmutableList.of()) //TODO: implement
                .finished(false)
                .build();

    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final Respondent respondent) {
        return QuestionnaireOutput.builder()
                .id(respondent.getId().getValue())
                .catalysts(ImmutableList.of()) //TODO: implement
                .finished(false)
                .build();

    }
}
