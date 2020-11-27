package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.answer.CatalystAnswerId;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CatalystOpenQuestionAnswerRepository extends JpaRepository<CatalystOpenQuestionAnswer, CatalystAnswerId> {
    @NonNull
    @EntityGraph("open-catalyst-answer-graph")
    List<CatalystOpenQuestionAnswer> findAllByIdSurveyIdAndIdRespondentId(@NonNull UUID surveyId, @NonNull UUID respondentId);
}
