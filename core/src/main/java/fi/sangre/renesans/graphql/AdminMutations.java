package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminMutations implements GraphQLMutationResolver {
    private final OrganizationService organizationService;

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public Organization storeOrganization(@NonNull final OrganizationInput input) {
        return organizationService.storeOrganization(input);
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public Organization removeOrganization(@NonNull final UUID id) {
        return organizationService.softDeleteOrganization(id);
    }
}
