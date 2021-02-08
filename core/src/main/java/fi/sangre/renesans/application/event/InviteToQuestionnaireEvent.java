package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

@RequiredArgsConstructor
@Data
@ToString
public class InviteToQuestionnaireEvent {
    private final SurveyId surveyId;
    private final String subject;
    private final String body;
    private final Set<RespondentId> respondentIds;
    /**
     * name, email pair
     */
    private final Pair<String, String> replyTo;
}
