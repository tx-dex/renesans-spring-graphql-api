package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.answer.CatalystAnswerId;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CatalystOpenQuestionAnswerRepository extends JpaRepository<CatalystOpenQuestionAnswerEntity, CatalystAnswerId> {
    @NonNull
    @EntityGraph("catalyst-answer-graph")
    List<CatalystOpenQuestionAnswerEntity> findAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);
    @NonNull
    List<CatalystOpenQuestionAnswerEntity> findAllByIdSurveyId(@NonNull UUID surveyId);

    long countAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);

    @NonNull
    List<CatalystOpenQuestionAnswerEntity> findAllByIdSurveyIdAndCatalystIdAndIdRespondentIdInOrderByAnswerTimeDesc(@NonNull UUID surveyId
            , @NonNull UUID catalystId
            , @NonNull Set<UUID> respondentIds);

    @NonNull
    List<CatalystOpenQuestionAnswerEntity> findAllByIdSurveyIdAndCatalystIdAndIsPublicIsTrueAndIdRespondentIdInOrderByAnswerTimeDesc(@NonNull UUID surveyId
            , @NonNull UUID catalystId
            , @NonNull Set<UUID> respondentIds);

    void deleteAllByRespondent(@NonNull SurveyRespondent respondent);
}
