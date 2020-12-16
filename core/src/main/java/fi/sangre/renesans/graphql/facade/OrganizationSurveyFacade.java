package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.dao.OrganizationDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.assemble.OrganizationSurveyOutputAssembler;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    public Collection<OrganizationOutput> getOrganizations() {
        try {
            final Future<Map<OrganizationId, RespondentCounters>> respondents = organizationService.countRespondentsAsync();
            final Future<Map<OrganizationId, SurveyCounters>> surveys = organizationService.countSurveysAsync();
            final List<Organization> organizations = organizationService.findAll();

            return organizationOutputAssembler.from(organizations, respondents.get(), surveys.get());
        } catch (final ExecutionException | InterruptedException ex) {
            log.warn("Cannot get organizations", ex);
            throw new InternalServiceException("Cannot get organizations");
        }
    }

    public OrganizationOutput getOrganization(@NonNull final OrganizationId id) {
        return organizationOutputAssembler.from(organizationDao.getOrganizationOrThrow(id));
    }

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
}
