package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.MultilingualPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultilingualPhraseRepository extends JpaRepository<MultilingualPhrase, Long> {

    @Query("SELECT DISTINCT locale FROM MultilingualPhrase")
    List<String> findLocales();

}
