package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByUsersIdIn(Set<Long> userId);
    List<Role> findByNameIn(List<String> names);
}


