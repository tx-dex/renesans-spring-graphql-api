package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.answer.CatalystAnswerId;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CatalystOpenQuestionAnswerRepository extends JpaRepository<CatalystOpenQuestionAnswerEntity, CatalystAnswerId> {
    @NonNull
    @EntityGraph("catalyst-answer-graph")
    List<CatalystOpenQuestionAnswerEntity> findAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);
}
