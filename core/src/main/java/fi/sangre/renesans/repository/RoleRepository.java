package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Role;
import fi.sangre.renesans.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByUsersContaining(User u);
    List<Role> findByNameIn(List<String> names);
}


