package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatistics;
import fi.sangre.renesans.graphql.output.statistics.AfterGameDriverStatistics;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;

    @NonNull
    public Collection<AfterGameCatalystStatistics> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey;
        if (principal instanceof RespondentPrincipal) {
            survey = organizationSurveyService.getSurvey(((RespondentPrincipal) principal).getSurveyId());
        } else if (principal instanceof UserPrincipal) {
            survey = organizationSurveyService.getSurvey(questionnaireId);
        } else {
            throw new SurveyException("Invalid principal");
        }

        final QuestionnaireOutput questionnaire = questionnaireAssembler.from(survey);

        return questionnaire.getCatalysts().stream()
                .map(catalyst -> AfterGameCatalystStatistics.builder()
                        .catalyst(catalyst)
                        .respondentRate(0d)
                        .respondentGroupRate(0d)
                        .drivers(catalyst.getDrivers().stream()
                                .map(driver -> AfterGameDriverStatistics.builder()
                                        .driver(driver)
                                        .respondentRate(0d)
                                        .respondentGroupRate(0d)
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
