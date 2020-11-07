package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Sort DEFAULT_USER_SORTING = new Sort(
            new Sort.Order(Sort.Direction.ASC, "firstName").ignoreCase(),
            new Sort.Order(Sort.Direction.ASC, "lastName").ignoreCase()
    );

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Set<User> findByCustomersContaining(Customer customer, Sort sort);

    default Set<User> findByCustomersContaining(Customer customer) {
        return findByCustomersContaining(customer, DEFAULT_USER_SORTING);
    }

    List<User> findAll(Sort sort);

    @Override
    default List<User> findAll() {
        return findAll(DEFAULT_USER_SORTING);
    }

    //DO NOT USE IT ANYWHERE BESIDES AUTHORIZATION
    @Query(value = "SELECT customer_id FROM dataserver.customers_users where user_id = :id " +
            "UNION ALL SELECT id FROM dataserver.customer WHERE created_by = :id", nativeQuery = true)
    Set<BigInteger> findCustomerIdsAccessibleByUserId(@Param("id") Long userId);

    @Query(value = "SELECT DISTINCT RG.id FROM dataserver.respondent_group RG INNER JOIN " +
            "  (SELECT customer_id FROM dataserver.customers_users  WHERE user_id = :id " +
            "   UNION ALL SELECT id FROM dataserver.customer WHERE created_by = :id) C ON C.customer_id = RG.customer_id", nativeQuery = true)
    Set<String> findRespondentGroupIdsAccessibleByUserId(@Param("id") Long userId);

    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdIsNot(String email, Long id);
    Boolean existsByUsername(String username);
    Boolean existsByUsernameAndIdIsNot(String username, Long id);
}


