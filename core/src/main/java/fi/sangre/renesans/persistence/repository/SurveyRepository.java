package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Survey;
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
public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    Optional<Survey> findByIsDefaultTrue();

    List<Survey> findAllByOrganisationsIdIn(@NonNull Set<UUID> organizationIds);

    @Deprecated
    Survey findByRespondentGroupsContaining(RespondentGroup respondentGroup);

    @Deprecated
    @Query("SELECT s FROM Survey s, RespondentGroup g WHERE g.survey.id = s.id AND g.id = :id")
    Survey findByRespondentGroupId(@Param("id") String id);

    @Deprecated
    @Query("SELECT s FROM Survey s, RespondentGroup g, Respondent r WHERE g.survey.id = s.id AND g.id = r.respondentGroup.id AND r.id = :id")
    Survey findByRespondentId(@Param("id") String id);
}
