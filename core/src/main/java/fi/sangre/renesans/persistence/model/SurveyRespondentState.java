package fi.sangre.renesans.persistence.model;

public enum SurveyRespondentState {
    INVITING,
    OPENED,
    ANSWERING_PARAMETERS,
    ANSWERED_PARAMETERS,
    OPENED_QUESTIONS,
    ANSWERING,
    ANSWERED,
    OPENED_AFTER_GAME,
    CANCELLED,
    ERROR;
}
