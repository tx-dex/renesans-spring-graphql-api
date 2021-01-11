package fi.sangre.renesans.application.model.answer;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class OpenQuestionAnswer {
    private QuestionId id;
    private CatalystId catalystId;
    private AnswerStatus status;
    @Accessors(fluent = true)
    private boolean isPublic;
    private String response;
}
