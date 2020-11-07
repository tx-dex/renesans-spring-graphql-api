package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.RespondentOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespondentOptionRepository extends JpaRepository<RespondentOption,Long> {
    List<RespondentOption> findByOptionType(RespondentOption.OptionType optionType);
}


