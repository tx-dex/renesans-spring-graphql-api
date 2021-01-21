package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.answer.LikertQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.QuestionAnswerId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LikerQuestionAnswerRepository extends JpaRepository<LikertQuestionAnswerEntity, QuestionAnswerId> {
    @NonNull
    @EntityGraph("question-answer-graph")
    List<LikertQuestionAnswerEntity> findAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);

    long countAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.QuestionStatistics(id.questionId, AVG(response), MIN(response), MAX(response), COUNT(id.questionId), AVG(rate)) " +
            "FROM LikertQuestionAnswerEntity " +
            "WHERE id.surveyId = :surveyId AND id.respondentId in :respondentIds " +
            "GROUP BY id.questionId")
    List<QuestionStatistics> getQuestionStatisticsByQuestionAndRespondentsIn(@Param("surveyId") UUID surveyId, @Param("respondentIds") Set<UUID> respondentIds);

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.QuestionStatistics(id.questionId, AVG(response), MIN(response), MAX(response), COUNT(id.questionId), AVG(rate)) " +
            "FROM LikertQuestionAnswerEntity " +
            "WHERE id.surveyId = :surveyId " +
            "GROUP BY id.questionId")
    List<QuestionStatistics> getQuestionStatisticsByQuestion(@Param("surveyId") UUID surveyId);

    void deleteAllByRespondent(@NonNull SurveyRespondent respondent);
}
