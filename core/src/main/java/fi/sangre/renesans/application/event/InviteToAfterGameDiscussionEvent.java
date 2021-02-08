package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Data
@ToString
public class InviteToAfterGameDiscussionEvent {
    private final SurveyId surveyId;
    private final QuestionId questionId;
    private final String subject;
    private final String body;
    private final Set<? extends IdValueObject<UUID>> respondentIds;
    /**
     * name, email pair
     */
    private final Pair<String, String> replyTo;
}
