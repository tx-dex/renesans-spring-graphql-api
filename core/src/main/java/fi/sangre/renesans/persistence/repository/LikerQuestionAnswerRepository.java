package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.answer.LikertQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.QuestionAnswerId;
import fi.sangre.renesans.persistence.model.statistics.AnswerDistribution;
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

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.QuestionStatistics(id.questionId, AVG(response), MIN(response), MAX(response), COUNT(id.questionId), AVG(rate), SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END)) " +
            "FROM LikertQuestionAnswerEntity " +
            "WHERE id.surveyId = :surveyId AND id.respondentId in :respondentIds " +
            "GROUP BY id.questionId")
    List<QuestionStatistics> getQuestionStatisticsByQuestionAndRespondentsIn(@Param("surveyId") UUID surveyId, @Param("respondentIds") Set<UUID> respondentIds);

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.QuestionStatistics(a.id.questionId, AVG(a.response), MIN(a.response), MAX(a.response), COUNT(a.id.questionId), AVG(a.rate), SUM(CASE WHEN a.status = 2 THEN 1 ELSE 0 END)) " +
            "FROM LikertQuestionAnswerEntity a " +
            "LEFT JOIN a.respondent r " +
            "WHERE a.id.surveyId = :surveyId " +
            "AND r.state = 'ANSWERED' " +
            "GROUP BY a.id.questionId")
    List<QuestionStatistics> getQuestionStatisticsByQuestion(@Param("surveyId") UUID surveyId);

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.AnswerDistribution(CAST(a.response as string), COUNT(a.response)) " +
            "FROM LikertQuestionAnswerEntity a " +
            "WHERE a.id.questionId = :questionId " +
            "AND a.id.surveyId = :surveyId " +
            "AND a.id.respondentId in :respondentIds " +
            "AND a.status = 1 " +
            "GROUP BY a.response")
    List<AnswerDistribution> getQuestionResponseDistributionByRespondentsIn(@Param("surveyId") UUID surveyId, @Param("questionId") UUID questionId, @Param("respondentIds") Set<UUID> respondentIds);

    @Query("SELECT new fi.sangre.renesans.persistence.model.statistics.AnswerDistribution(CAST(a.rate as string), COUNT(a.response)) " +
            "FROM LikertQuestionAnswerEntity a " +
            "WHERE a.id.questionId = :questionId " +
            "AND a.id.surveyId = :surveyId " +
            "AND a.id.respondentId in :respondentIds " +
            "AND a.status = 1 " +
            "GROUP BY a.rate")
    List<AnswerDistribution> getQuestionRateDistributionByRespondentsIn(@Param("surveyId") UUID surveyId, @Param("questionId") UUID questionId, @Param("respondentIds") Set<UUID> respondentIds);

    void deleteAllByRespondent(@NonNull SurveyRespondent respondent);
}
