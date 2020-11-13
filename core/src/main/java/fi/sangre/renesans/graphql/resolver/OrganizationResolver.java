package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import static fi.sangre.renesans.application.model.Counts.ZERO_L;

@RequiredArgsConstructor

@Component
public class OrganizationResolver implements GraphQLResolver<Organization> {
    private final OrganizationService organizationService;

    @Nullable
    public Segment getSegment(@NonNull final Organization organization) {
        return organizationService.getSegment(organization);
    }

    @NonNull
    public OrganizationSurvey getSurvey(@NonNull final Organization organization) {
        return organizationService.getSurvey(organization);
    }

    @NonNull
    public Long getSurveyCount(@NonNull final Organization organization) {
        // TODO: implement
        return ZERO_L;
    }

    @NonNull
    public RespondentCounters getRespondentCounts(@NonNull final Organization organization) {
        // TODO: implement
        return RespondentCounters.builder().build();
    }
}
