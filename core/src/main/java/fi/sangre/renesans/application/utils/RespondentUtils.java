package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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
            return isInvited(respondent.getState());
        }
    }

    public boolean isInvited(@NonNull final SurveyRespondentState state) {
        return state != SurveyRespondentState.ERROR
                && state != SurveyRespondentState.INVITING;
    }

    public boolean isOpened(@NonNull final SurveyRespondentState state) {
        return state == SurveyRespondentState.OPENED;
    }

    public boolean isAnsweringParameters(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            return isAnsweringParameters(respondent.getState());
        }
    }

    public boolean isAnsweringParameters(@NonNull final SurveyRespondentState state) {
        return state == SurveyRespondentState.ANSWERING_PARAMETERS
                || state == SurveyRespondentState.ANSWERED_PARAMETERS;
    }

    public boolean isAnsweringQuestions(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            return isAnsweringQuestions(respondent.getState());
        }
    }

    public boolean isAnsweringQuestions(@NonNull final SurveyRespondentState state) {
        return state == SurveyRespondentState.ANSWERING
                || state == SurveyRespondentState.ANSWERED
                || state == SurveyRespondentState.OPENED_QUESTIONS;
    }

    public boolean isViewingAfterGame(@NonNull final SurveyRespondentState state) {
        return state == SurveyRespondentState.OPENED_AFTER_GAME;
    }

    public boolean isAnswered(@Nullable final SurveyRespondent respondent) {
        if (respondent == null) {
            return false;
        } else {
            return isAnswered(respondent.getState());
        }
    }

    public boolean isAnswered(@NonNull final SurveyRespondentState state) {
        return state == SurveyRespondentState.ANSWERED;
    }
}
