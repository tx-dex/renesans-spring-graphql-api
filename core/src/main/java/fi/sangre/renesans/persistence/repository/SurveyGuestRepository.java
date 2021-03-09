package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SurveyGuestRepository extends JpaRepository<SurveyGuest, UUID>, QuerydslPredicateExecutor<SurveyGuest> {
    @NonNull
    List<SurveyGuest> findAllBySurveyId(@NonNull UUID surveyId);
    @NonNull
    List<SurveyGuest> findAllBySurveyIdAndEmailIn(@NonNull UUID surveyId, @NonNull Set<String> emails);

    @NonNull
    Optional<SurveyGuest> findByIdAndInvitationHash(@NonNull UUID id, @NonNull String invitationHash);
}
