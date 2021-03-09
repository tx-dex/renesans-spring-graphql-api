package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.SurveyId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class RespondentParameterAnswerEvent {
    private final SurveyId surveyId;
    private final IdValueObject<UUID> respondentId;
}
