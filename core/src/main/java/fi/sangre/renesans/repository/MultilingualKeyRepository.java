package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.MultilingualKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface MultilingualKeyRepository extends JpaRepository<MultilingualKey, Long> {

    Optional<MultilingualKey> findById(Long id);
    MultilingualKey findByKey(String key);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<MultilingualKey> findAllByKeyIn(List<String> key);
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<MultilingualKey> findAllByKeyStartsWith(String startsWith);
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<MultilingualKey> findAllByKeyInOrKeyStartsWith(List<String> keys, String startsWith);
}
