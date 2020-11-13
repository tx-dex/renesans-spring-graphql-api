package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminQueries implements GraphQLQueryResolver {
    private final OrganizationService organizationService;

    public List<Organization> getOrganizations() {
        return organizationService.findAll();
    }
}
