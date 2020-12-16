package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.RespondentStateCounters;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
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
public interface SurveyRespondentRepository extends JpaRepository<SurveyRespondent, UUID> {
    @NonNull
    List<SurveyRespondent> findAllBySurveyId(@NonNull UUID surveyId);

    @NonNull
    Optional<SurveyRespondent> findByIdAndInvitationHash(@NonNull UUID id, @NonNull String invitationHash);

    @Query("SELECT new fi.sangre.renesans.persistence.model.RespondentStateCounters(p.surveyId, " +
            "sum(CASE WHEN p.state = 'OPENED' THEN 1 ELSE 0 END), " +
            "sum(CASE WHEN p.state = 'ANSWERING' THEN 1 ELSE 0 END), " +
            "sum(CASE WHEN p.state = 'ANSWERED' THEN 1 ELSE 0 END), " +
            "count(p)) " +
            "FROM SurveyRespondent p " +
            "GROUP BY p.surveyId")
    List<RespondentStateCounters> countSurveyRespondents();

    @Query("SELECT new fi.sangre.renesans.persistence.model.RespondentStateCounters(p.surveyId, " +
            "sum(CASE WHEN p.state = 'OPENED' THEN 1 ELSE 0 END), " +
            "sum(CASE WHEN p.state = 'ANSWERING' THEN 1 ELSE 0 END), " +
            "sum(CASE WHEN p.state = 'ANSWERED' THEN 1 ELSE 0 END), " +
            "count(p)) " +
            "FROM SurveyRespondent p " +
            "WHERE p.surveyId IN :surveyIds " +
            "GROUP BY p.surveyId")
    List<RespondentStateCounters> countSurveyRespondents(@Param("surveyIds") @NonNull Set<UUID> surveyIds);

}
