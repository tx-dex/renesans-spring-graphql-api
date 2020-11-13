package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.CustomerDriverWeights;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerDriverWeightsRepository extends JpaRepository<CustomerDriverWeights, Long> {

    @NonNull
    List<CustomerDriverWeights> findAllByCustomerId(@NonNull UUID customerId);
}