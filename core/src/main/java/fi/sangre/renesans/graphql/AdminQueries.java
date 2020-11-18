package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminQueries implements GraphQLQueryResolver {
    private final OrganizationService organizationService;
    private final OrganizationSurveyService organizationSurveyService;

    @NonNull
    // TODO: authorize
    public List<Organization> getOrganizations() {
        return organizationService.findAll();
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'organization', 'READ')") //TODO: fix in the permission resolver
    public Organization getOrganization(@NonNull final UUID id) {
        return organizationService.findOrganization(id);
    }

    @NonNull
    // TODO: authorize
    public OrganizationSurvey getOrganizationSurvey(@NonNull final UUID id) {
        return organizationSurveyService.getSurvey(id);
    }
}
