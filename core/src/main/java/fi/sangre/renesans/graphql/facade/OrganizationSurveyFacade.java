package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.OrganizationDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.assemble.OrganizationSurveyOutputAssembler;
import fi.sangre.renesans.graphql.assemble.statistics.SurveyCatalystStatisticsAssembler;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyCatalystStatisticsOutput;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.statistics.RespondentStatisticsService;
import fi.sangre.renesans.service.statistics.SurveyStatisticsService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final SurveyDao surveyDao;
    private final AnswerDao answerDao;
    private final OrganizationSurveyOutputAssembler organizationSurveyOutputAssembler;
    private final OrganizationService organizationService;
    private final OrganizationDao organizationDao;
    private final OrganizationOutputAssembler organizationOutputAssembler;
    private final SurveyStatisticsService surveyStatisticsService;
    private final RespondentStatisticsService respondentStatisticsService;
    private final SurveyCatalystStatisticsAssembler surveyCatalystStatisticsAssembler;
    private final SurveyUtils surveyUtils;
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public OrganizationSurvey copySurvey(@NonNull final OrganizationId organizationId,
                                         @NonNull final SurveyInput input,
                                         @NonNull final String languageTag) {
        checkArgument(input.getSourceSurveyId() != null, "SourceSurvetId is required");

        final SurveyId sourceId = new SurveyId(input.getSourceSurveyId());

        final MultilingualText titles = multilingualUtils.create(input.getTitle(), languageTag);
        final MultilingualText descriptions = multilingualUtils.create(input.getDescription(), languageTag);

        return organizationSurveyService.copySurvey(organizationId, sourceId, titles, descriptions);
    }


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
    public OrganizationSurvey enableAfterGame(@NonNull final SurveyId surveyId, @NonNull final Long version) {
        try {
            return surveyDao.enableAfterGame(surveyId, version);
        } catch (final Exception ex) {
            log.warn("Cannot enable after game for survey(id={})", surveyId, ex);
            throw new SurveyException("Cannot enable after game");
        }
    }

    @NonNull
    public SurveyCatalystStatisticsOutput getStatistics(@NonNull final SurveyId surveyId,
                                                        @NonNull final CatalystId catalystId,
                                                        @NonNull final List<RespondentFilter> filters,
                                                        @NonNull final String languageTag) {

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);
        final Catalyst catalyst = Optional.ofNullable(surveyUtils.findCatalyst(catalystId, survey))
                .orElseThrow(() -> new SurveyException("Catalyst not found"));

        final SurveyResult statistics;
        if (filters.isEmpty()) {
            statistics = surveyStatisticsService.calculateStatistics(survey);
        } else  {
            //TODO: We can verify also if the filter contains only one parameter. in that case we could use parameterStatisticsService which caches data
            statistics =  respondentStatisticsService.calculateStatistics(survey, filters);
        }

        final SurveyCatalystStatisticsOutput output = surveyCatalystStatisticsAssembler.from(catalyst, statistics, languageTag);

        if (output.getOpenQuestion() != null) {
            Try.ofSupplier(() -> answerDao.getAllOpenQuestionAnswers(surveyId, statistics.getRespondentIds()))
                    .onSuccess(answers -> output.getOpenQuestion().setAnswers(answers))
                    .onFailure(ex -> log.warn("Cannot get answers", ex))
                    .getOrElseThrow(ex -> new SurveyException("Cannot get answers"));
        }

        return output;
    }
}
