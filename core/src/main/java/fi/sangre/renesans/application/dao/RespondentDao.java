package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.RespondentUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentDao {
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final RespondentUtils respondentUtils;

    /**
     * @param respondentId Respondent Id
     * @return true if is answering or already answered all, as user maybe can change the answers
     */
    @Transactional(readOnly = true)
    public boolean isAnswering(@NonNull final RespondentId respondentId) {
        return respondentUtils.isAnswering(getRespondentOrThrow(respondentId));
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
    public void updateRespondentStatus(@NonNull final RespondentId respondentId, @NonNull final SurveyRespondentState newStatus) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        respondent.setState(newStatus);
        surveyRespondentRepository.save(respondent);
    }

    private SurveyRespondent getRespondentOrThrow(@NonNull final RespondentId id) {
        return surveyRespondentRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));
    }

    @Transactional(readOnly = true)
    public boolean isConsented(@NonNull final RespondentId id) {
        return surveyRespondentRepository.findById(id.getValue())
                .map(SurveyRespondent::getConsent)
                .orElse(false);
    }

    @Transactional
    public void consent(@NonNull final RespondentId id, @NonNull final Boolean consent) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        if (!respondent.getConsent().equals(consent)) {
            surveyRespondentRepository.save(respondent);
        }
    }
}
