package fi.sangre.renesans.service;

import fi.sangre.renesans.model.Role;
import fi.sangre.renesans.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Service
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(final RoleRepository roleRepository) {
        checkArgument(roleRepository != null, "RoleRepository is required");

        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    public List<Role> findByNames(List<String> names) {
        return roleRepository.findByNameIn(names);
    }
}
