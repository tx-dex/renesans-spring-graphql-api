package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.dao.OrganizationDao;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.graphql.output.aaa.UserOutput;
import fi.sangre.renesans.persistence.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class UserResolver implements GraphQLResolver<UserOutput> {
    private final OrganizationDao organizationDao;
    private final OrganizationOutputAssembler organizationOutputAssembler;

    @Deprecated
    public List<Customer> getCustomers(@NonNull final UserOutput user) {
        return ImmutableList.of();
    }

    public Collection<OrganizationOutput> getOrganizations(@NonNull final UserOutput user) {
        return organizationOutputAssembler.from(
                organizationDao.getUserOrganizations(user.getId()));
    }
}
