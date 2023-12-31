package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.application.assemble.RespondentAssembler;
import fi.sangre.renesans.application.model.QuestionnaireUserState;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.RespondentUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.repository.CatalystOpenQuestionAnswerRepository;
import fi.sangre.renesans.persistence.repository.LikerQuestionAnswerRepository;
import fi.sangre.renesans.persistence.repository.ParameterAnswerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentDao {
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final LikerQuestionAnswerRepository likerQuestionAnswerRepository;
    private final CatalystOpenQuestionAnswerRepository catalystOpenQuestionAnswerRepository;
    private final ParameterAnswerRepository parameterAnswerRepository;
    private final RespondentUtils respondentUtils;
    private final RespondentAssembler respondentAssembler;

    @Transactional(readOnly = true)
    public QuestionnaireUserState getState(@NonNull final RespondentId id) {
        return getRespondent(id, entity -> QuestionnaireUserState.builder()
                .consented(Boolean.TRUE.equals(entity.getConsent()))
                .answeringParameters(respondentUtils.isAnsweringParameters(entity.getState()))
                .answeringQuestions(respondentUtils.isAnsweringQuestions(entity.getState()))
                .viewingAfterGame(respondentUtils.isViewingAfterGame(entity.getState()))
                .languageTag(Optional.ofNullable(entity.getSelectedLanguageTag())
                        .orElse(entity.getInvitationLanguageTag()))
                .build());
    }

    /**
     * @param respondentId Respondent Id
     * @return true if is answering or already answered all, as user maybe can change the answers
     */
    @Transactional(readOnly = true)
    public boolean isAnswering(@NonNull final RespondentId respondentId) {
        return respondentUtils.isAnsweringQuestions(getRespondentOrThrow(respondentId));
    }

    /**
     * @param respondentId Respondent Id
     * @return true if respondent already opened the survey or started answering questions;
     */
    @Transactional(readOnly = true)
    public boolean isInvited(@NonNull final RespondentId respondentId) {
        return respondentUtils.isInvited(getRespondentOrThrow(respondentId));
    }

    @Transactional
    public void updateRespondentError(@NonNull final RespondentId respondentId, @Nullable String error) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        respondent.setInvitationError(error);

        surveyRespondentRepository.save(respondent);
    }

    @Transactional
    public void updateRespondentStatus(@NonNull final RespondentId respondentId, @NonNull final SurveyRespondentState newStatus) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        final SurveyRespondentState currentStatus = respondent.getState();

        if (!currentStatus.equals(newStatus)) {
            if (SurveyRespondentState.OPENED.equals(newStatus)) {
                if (!respondentUtils.isInvited(currentStatus)) {
                    respondent.setState(newStatus);
                }
            } else if (SurveyRespondentState.ANSWERING_PARAMETERS.equals(newStatus)) {
                if (respondentUtils.isOpened(currentStatus)) {
                    respondent.setState(newStatus);
                }
            } else if (SurveyRespondentState.ANSWERED_PARAMETERS.equals(newStatus)) {
                if (!respondentUtils.isAnsweringQuestions(currentStatus)) {
                    respondent.setState(newStatus);
                }
            } else if (SurveyRespondentState.OPENED_QUESTIONS.equals(newStatus)) {
                if (!respondentUtils.isAnsweringQuestions(currentStatus)) {
                    respondent.setState(newStatus);
                }
            } else if (SurveyRespondentState.ANSWERING.equals(newStatus)) {
                if (!respondentUtils.isAnswered(currentStatus)) {
                    respondent.setState(newStatus);
                }
            } else {
                respondent.setState(newStatus);
            }

            surveyRespondentRepository.save(respondent);
        }
    }

    @Nullable
    @Transactional(readOnly = true)
    public RespondentId findRespondent(@NonNull final RespondentId respondentId, @NonNull final String invitationHash) {
        return surveyRespondentRepository.findByIdAndInvitationHash(respondentId.getValue(), invitationHash)
                .map(v -> new RespondentId(v.getId()))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Set<RespondentId> findRespondentsByEmails(@NonNull final SurveyId surveyId, @NonNull final Set<String> emails) {
        return surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .filter(e -> emails.contains(e.getEmail()))
                .map(e -> new RespondentId(e.getId()))
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional(readOnly = true)
    public Set<RespondentId> findRespondentsNotInState(@NonNull final SurveyId surveyId, @NonNull final SurveyRespondentState state) {
        return surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .filter(e -> e.getState() != state)
                .map(e -> new RespondentId(e.getId()))
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional(readOnly = true)
    public Set<RespondentId> findRespondents(@NonNull final SurveyId surveyId) {
        return surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .map(e -> new RespondentId(e.getId()))
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional(readOnly = true)
    public <T> Map<RespondentId, T> findRespondents(@NonNull final SurveyId surveyId,
                                                    @NonNull final Function<SurveyRespondent, T> mapper) {
        return surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .collect(collectingAndThen(toMap(v -> new RespondentId(v.getId()), mapper), Collections::unmodifiableMap));
    }

    @Transactional(readOnly = true)
    public <T> Map<RespondentId, T> findActiveRespondents(@NonNull final SurveyId surveyId,
                                                    @NonNull final Function<SurveyRespondent, T> mapper) {
        return surveyRespondentRepository.findAllActiveBySurveyId(surveyId.getValue()).stream()
                .collect(collectingAndThen(toMap(v -> new RespondentId(v.getId()), mapper), Collections::unmodifiableMap));
    }

    private SurveyRespondent getRespondentOrThrow(@NonNull final RespondentId id) {
        return surveyRespondentRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));
    }

    @NonNull
    @Transactional(readOnly = true)
    public <T> T getRespondent(@NonNull final RespondentId id, @NonNull final Function<SurveyRespondent, T> mapper) {
        return surveyRespondentRepository.findById(id.getValue())
                .map(mapper)
                .orElseThrow(() -> new SurveyException("Respondent not found"));
    }

    @Transactional
    public void consent(@NonNull final RespondentId id, @NonNull final Boolean consent) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        if (!respondent.getConsent().equals(consent)) {
            respondent.setConsent(consent);
            surveyRespondentRepository.save(respondent);
        }
    }

    @NonNull
    @Transactional
    public Respondent softDeleteRespondent(@NonNull final RespondentId respondentId) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        likerQuestionAnswerRepository.deleteAllByRespondent(respondent);
        catalystOpenQuestionAnswerRepository.deleteAllByRespondent(respondent);
        parameterAnswerRepository.deleteAllByRespondent(respondent);
        surveyRespondentRepository.delete(respondent);

        return respondentAssembler.from(respondent);
    }

    public void disableRespondentInvitation(@NonNull final RespondentId respondentId) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        respondent.setState(SurveyRespondentState.CANCELLED);
        surveyRespondentRepository.save(respondent);
    }
}
