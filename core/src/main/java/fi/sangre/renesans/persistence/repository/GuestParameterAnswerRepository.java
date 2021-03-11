package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.persistence.model.SurveyGuest;
import fi.sangre.renesans.persistence.model.answer.GuestParameterAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerId;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GuestParameterAnswerRepository extends JpaRepository<GuestParameterAnswerEntity, ParameterAnswerId>, QuerydslPredicateExecutor<GuestParameterAnswerEntity> {
    @NonNull
    @EntityGraph("guest-parameter-answer-graph")
    List<GuestParameterAnswerEntity> findAllByIdSurveyIdAndTypeIs(@NonNull UUID surveyId,
                                                                  @NonNull ParameterAnswerType type);

    @NonNull
    @EntityGraph("guest-parameter-answer-graph")
    List<GuestParameterAnswerEntity> findAllByIdSurveyIdAndIdGuestIdAndTypeIs(@NonNull UUID surveyId,
                                                                              @NonNull UUID guestId,
                                                                              @NonNull ParameterAnswerType type);

    long countByIdSurveyIdAndIdGuestIdAndTypeIs(@NonNull UUID surveyId,
                                                @NonNull UUID guestId,
                                                @NonNull ParameterAnswerType type);


    void deleteAllByIdSurveyIdAndIdGuestIdAndRootId(@NonNull UUID surveyId,
                                                    @NonNull UUID guestId,
                                                    @NonNull UUID rootId);

    void deleteAllByGuest(@NonNull SurveyGuest guest);
}
