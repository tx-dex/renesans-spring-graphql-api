package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentUtils {
    public boolean isInvited(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            final SurveyRespondentState state = respondent.getState();
            return state == SurveyRespondentState.OPENED
                    || state == SurveyRespondentState.ANSWERING
                    || state == SurveyRespondentState.ANSWERED;
        }
    }

    public boolean isAnswering(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            final SurveyRespondentState state = respondent.getState();
            return state == SurveyRespondentState.ANSWERING
                    || state == SurveyRespondentState.ANSWERED;
        }
    }

    public boolean isAnswered(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            final SurveyRespondentState state = respondent.getState();
            return state == SurveyRespondentState.ANSWERED;
        }
    }
}
