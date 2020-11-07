package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.CustomerDriverWeights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDriverWeightsRepository extends JpaRepository<CustomerDriverWeights, Long> {

    List<CustomerDriverWeights> findAllByCustomerId(Long customerId);
}