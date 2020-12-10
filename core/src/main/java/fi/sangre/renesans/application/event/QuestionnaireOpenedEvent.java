package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class QuestionnaireOpenedEvent {
    private final RespondentId respondentId;
}
