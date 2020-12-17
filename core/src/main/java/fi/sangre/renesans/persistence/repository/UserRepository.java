package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Sort DEFAULT_USER_SORTING = new Sort(
            new Sort.Order(Sort.Direction.ASC, "firstName").ignoreCase(),
            new Sort.Order(Sort.Direction.ASC, "lastName").ignoreCase()
    );

    @Override
    @NonNull
    @EntityGraph("user-roles-graph")
    Optional<User> findById(@NonNull Long id);

    @Override
    @NonNull
    @EntityGraph("user-roles-graph")
    List<User> findAll(@NonNull Sort sort);

    @Override
    @NonNull
    @EntityGraph("user-roles-graph")
    default List<User> findAll() {
        return findAll(DEFAULT_USER_SORTING);
    }

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Set<User> findByCustomersContaining(Customer customer, Sort sort);

    default Set<User> findByCustomersContaining(Customer customer) {
        return findByCustomersContaining(customer, DEFAULT_USER_SORTING);
    }

    //DO NOT USE IT ANYWHERE BESIDES AUTHORIZATION
    @NonNull
    @Query(value = "SELECT o.id FROM Customer o WHERE o.createdBy = :id")
    Set<UUID> findOwnedOrganizationIds(@Param("id") @NonNull Long userId);

    @NonNull
    @Query(value = "SELECT o.id FROM User u " +
            "INNER JOIN u.customers o " +
            "WHERE u.id = :id")
    Set<UUID> findMappedOrganizationIds(@Param("id") @NonNull Long userId);



    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdIsNot(String email, Long id);
    Boolean existsByUsername(String username);
    Boolean existsByUsernameAndIdIsNot(String username, Long id);
}


