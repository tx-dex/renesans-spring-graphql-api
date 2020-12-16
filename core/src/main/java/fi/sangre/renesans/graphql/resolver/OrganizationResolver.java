package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.graphql.facade.OrganizationSurveyFacade;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.service.OrganizationService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static fi.sangre.renesans.application.model.Counts.ZERO_L;

@RequiredArgsConstructor

@Component
public class OrganizationResolver implements GraphQLResolver<OrganizationOutput> {
    private final OrganizationService organizationService;
    private final OrganizationSurveyFacade organizationSurveyFacade;
    private final ResolverHelper resolverHelper;

    @Nullable
    public Segment getSegment(@NonNull final OrganizationOutput organization) {
        return organizationService.getSegment(organization.getId());
    }

    @NonNull
    public Collection<OrganizationSurvey> getSurveys(@NonNull final OrganizationOutput organization, @NonNull final DataFetchingEnvironment environment) {
        return organizationSurveyFacade.getSurveys(new OrganizationId(organization.getId()), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public Long getSurveyCount(@NonNull final OrganizationOutput organization) {
        if (organization.getSurveyCounters() == null) {
            return ZERO_L; //TODO: implement
        } else {
            return organization.getSurveyCounters().getAll();
        }
    }

    @NonNull
    public RespondentCounters getRespondentCounts(@NonNull final OrganizationOutput organization) {
        if (organization.getRespondentCounters() == null) {
            return RespondentCounters.empty();// TODO: implement
        } else {
            return organization.getRespondentCounters();
        }
    }
}
