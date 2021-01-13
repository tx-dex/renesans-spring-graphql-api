package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.dao.OrganizationDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.assemble.OrganizationSurveyOutputAssembler;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyDriverStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyQuestionStatisticsOutput;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final SurveyDao surveyDao;
    private final OrganizationSurveyOutputAssembler organizationSurveyOutputAssembler;
    private final OrganizationService organizationService;
    private final OrganizationDao organizationDao;
    private final OrganizationOutputAssembler organizationOutputAssembler;

    @NonNull
    public Collection<OrganizationOutput> getOrganizations() {
        try {
            final Future<Map<OrganizationId, RespondentCounters>> respondents = organizationService.countRespondentsAsync();
            final Future<Map<OrganizationId, SurveyCounters>> surveys = organizationService.countSurveysAsync();
            final List<Organization> organizations = organizationDao.getAllOrganizations();

            return organizationOutputAssembler.from(organizations, respondents.get(), surveys.get());
        } catch (final ExecutionException | InterruptedException ex) {
            log.warn("Cannot get organizations", ex);
            throw new InternalServiceException("Cannot get organizations");
        }
    }

    @NonNull
    public OrganizationOutput getOrganization(@NonNull final OrganizationId id) {
        return organizationOutputAssembler.from(organizationDao.getOrganizationOrThrow(id));
    }

    @NonNull
    public Collection<OrganizationSurvey> getSurveys(@NonNull final OrganizationId organizationId,
                                                     @NonNull final String languageTag) {
        try {
            final Future<Map<SurveyId, RespondentCounters>> counters = organizationSurveyService.countRespondentsAsync(organizationId);
            final List<OrganizationSurvey> surveys = surveyDao.getSurveys(organizationId, languageTag);

            return organizationSurveyOutputAssembler.from(surveys, counters.get());
        } catch (final ExecutionException | InterruptedException ex) {
            log.warn("Cannot get surveys", ex);
            throw new InternalServiceException("Cannot get surveys for organization");
        }
    }

    @NonNull
    public Collection<SurveyCatalystStatisticsOutput> getStatistics(@NonNull final SurveyId surveyId,
                                                                    @NonNull final List<RespondentFilter> filters,
                                                                    @NonNull final String languageTag) {

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);

        return survey.getCatalysts().stream()
                .map(catalyst -> SurveyCatalystStatisticsOutput.builder()
                        .id(catalyst.getId().asString())
                        .title(MultilingualUtils.getText(catalyst.getTitles().getPhrases(), languageTag))
                        .result(0d)
                        .drivers(catalyst.getDrivers().stream()
                                .map(driver -> SurveyDriverStatisticsOutput.builder()
                                        .id(driver.getId().toString())
                                        .title(MultilingualUtils.getText(catalyst.getTitles().getPhrases(), languageTag))
                                        .result(0d)
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .questions(catalyst.getQuestions().stream()
                                .map(question -> SurveyQuestionStatisticsOutput.builder()
                                        .id(question.getId().asString())
                                        .title(MultilingualUtils.getText(question.getTitles().getPhrases(), languageTag))
                                        .result(0d)
                                        .rate(0d)
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
