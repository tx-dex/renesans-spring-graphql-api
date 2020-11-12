package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SegmentRepository extends JpaRepository<Segment, Long> {
    Sort DEFAULT_SEGMENT_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "name").ignoreCase());

    Optional<Segment> findById(Long id);
    Optional<Segment> findByCustomers(Customer customer);

    List<Segment> findAll(Sort sort);
    @Override
    default List<Segment> findAll() {
        return findAll(DEFAULT_SEGMENT_SORTING);
    }
}
