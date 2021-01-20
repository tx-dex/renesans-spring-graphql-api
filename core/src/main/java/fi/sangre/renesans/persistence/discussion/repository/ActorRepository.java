package fi.sangre.renesans.persistence.discussion.repository;

import fi.sangre.renesans.persistence.discussion.model.ActorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActorRepository extends JpaRepository<ActorEntity, Long> {
    @NonNull
    Optional<ActorEntity> findBySurveyIdAndRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);
}
