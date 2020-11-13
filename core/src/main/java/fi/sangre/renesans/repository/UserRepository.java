package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.User;
import fi.sangre.renesans.persistence.model.Customer;
import org.springframework.data.domain.Sort;
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

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Set<User> findByCustomersContaining(Customer customer, Sort sort);

    default Set<User> findByCustomersContaining(Customer customer) {
        return findByCustomersContaining(customer, DEFAULT_USER_SORTING);
    }

    @NonNull
    @Override
    default List<User> findAll() {
        return findAll(DEFAULT_USER_SORTING);
    }

    //DO NOT USE IT ANYWHERE BESIDES AUTHORIZATION
    @NonNull
    @Query(value = "SELECT customer_id FROM data.customers_users where user_id = :id " +
            "UNION ALL SELECT id FROM data.customer WHERE created_by = :id", nativeQuery = true)
    Set<UUID> findCustomerIdsAccessibleByUserId(@Param("id") @NonNull Long userId);

    @Query(value = "SELECT DISTINCT RG.id FROM data.respondent_group RG INNER JOIN " +
            "  (SELECT customer_id FROM data.customers_users  WHERE user_id = :id " +
            "   UNION ALL SELECT id FROM data.customer WHERE created_by = :id) C ON C.customer_id = RG.customer_id", nativeQuery = true)
    Set<String> findRespondentGroupIdsAccessibleByUserId(@Param("id") Long userId);

    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdIsNot(String email, Long id);
    Boolean existsByUsername(String username);
    Boolean existsByUsernameAndIdIsNot(String username, Long id);
}


