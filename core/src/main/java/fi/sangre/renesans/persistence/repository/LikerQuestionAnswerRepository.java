package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.answer.LikertQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.QuestionAnswerId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LikerQuestionAnswerRepository extends JpaRepository<LikertQuestionAnswerEntity, QuestionAnswerId> {
    @NonNull
    @EntityGraph("question-answer-graph")
    List<LikertQuestionAnswerEntity> findAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);

    long countAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);
}
