package fi.sangre.renesans.application.dao

import fi.sangre.renesans.application.model.respondent.RespondentId
import fi.sangre.renesans.application.utils.RespondentUtils
import fi.sangre.renesans.persistence.model.SurveyRespondent
import fi.sangre.renesans.persistence.model.SurveyRespondentState
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository
import spock.lang.Specification

class RespondentDaoTest extends Specification {
    final RESPONDENT_ID = new RespondentId(UUID.randomUUID())
    final surveyRespondentRepository = Mock(SurveyRespondentRepository)
    final respondentUtils = new RespondentUtils()
    final instance = new RespondentDao(surveyRespondentRepository, respondentUtils)

    def "should check that respondent is invited"() {
        when:
        def actual = instance.isInvited(RESPONDENT_ID)

        then:
        1 * surveyRespondentRepository.findById(RESPONDENT_ID.value) >> Optional.of(new SurveyRespondent(state: respondent_state))
        actual == expected

        where:
        respondent_state                | expected
        SurveyRespondentState.INVITING  | false
        SurveyRespondentState.OPENED    | true
        SurveyRespondentState.ANSWERING | true
        SurveyRespondentState.ANSWERED  | true
        SurveyRespondentState.ERROR     | false
    }

    def "should check that respondent is answering"() {
        when:
        def actual = instance.isAnswering(RESPONDENT_ID)

        then:
        1 * surveyRespondentRepository.findById(RESPONDENT_ID.value) >> Optional.of(new SurveyRespondent(state: respondent_state))
        actual == expected

        where:
        respondent_state                | expected
        SurveyRespondentState.INVITING  | false
        SurveyRespondentState.OPENED    | false
        SurveyRespondentState.ANSWERING | true
        SurveyRespondentState.ANSWERED  | true
        SurveyRespondentState.ERROR     | false
    }
}
