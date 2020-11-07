package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.repository.CustomerRepository;
import fi.sangre.renesans.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class UserResolver implements GraphQLResolver<User> {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserResolver(
            CustomerRepository customerRepository,
            RoleRepository roleRepository
    ) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
    }

    public List<Customer> getCustomers(User user) {
        return customerRepository.findByUsersContainingOrCreatedBy(user, user.getId());
    }

    public List<Role> getRoles(User user) {
        return roleRepository.findByUsersContaining(user);
    }
}
