package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SurveyRespondentRepository extends JpaRepository<SurveyRespondent, UUID> {
    @NonNull
    List<SurveyRespondent> findAllBySurveyId(@NonNull UUID surveyId);

    @NonNull
    Optional<SurveyRespondent> findByIdAndInvitationHash(@NonNull UUID id, @NonNull String invitationHash);
}
