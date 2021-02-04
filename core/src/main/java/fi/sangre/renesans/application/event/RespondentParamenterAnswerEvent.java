package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RespondentParamenterAnswerEvent {
    private final SurveyId surveyId;
    private final RespondentId respondentId;
}
