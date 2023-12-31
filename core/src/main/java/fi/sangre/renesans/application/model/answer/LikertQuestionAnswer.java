package fi.sangre.renesans.application.model.answer;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class LikertQuestionAnswer {
    private QuestionId id;
    private CatalystId catalystId;
    private AnswerStatus status;
    private Integer response;
    private Integer rate;
}
