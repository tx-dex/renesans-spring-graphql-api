package fi.sangre.renesans.application.model.answer;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class OpenQuestionAnswer {
    private QuestionId id;
    private CatalystId catalystId;
    private AnswerStatus status;
    private String response;
}
