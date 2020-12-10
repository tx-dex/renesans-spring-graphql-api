package fi.sangre.renesans.application.model.respondent;

public enum RespondentState {
    /**
     * When respondent was created in the db
     */
    INVITING,
    /** When email was sent to spam */
    SPAM_EMAIL,
    /** When email was sent */
    INVITED,
    /** When respondent opened questionnaire */
    OPENED,
    /** When respondent answered first question */
    ANSWERING,
    /** When respondent answered all questions */
    ANSWERED,
    /** When there was some kind of error either in sending email or storing respondent */
    ERROR;
}


