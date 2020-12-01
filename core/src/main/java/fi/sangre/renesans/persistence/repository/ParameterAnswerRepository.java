package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.answer.ParameterAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerId;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParameterAnswerRepository extends JpaRepository<ParameterAnswerEntity, ParameterAnswerId> {
    @NonNull
    @EntityGraph("parameter-answer-graph")
    List<ParameterAnswerEntity> findAllByIdSurveyIdAndTypeIs(@NonNull UUID surveyId,
                                                             @NonNull ParameterAnswerType type);

    @NonNull
    @EntityGraph("parameter-answer-graph")
    List<ParameterAnswerEntity> findAllByIdSurveyIdAndIdRespondentIdAndTypeIs(@NonNull UUID surveyId,
                                                                              @NonNull UUID respondentId,
                                                                              @NonNull ParameterAnswerType type);

    void deleteAllByRootId(@NonNull UUID rootId);
}
