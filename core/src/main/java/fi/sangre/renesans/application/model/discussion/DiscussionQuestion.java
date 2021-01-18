package fi.sangre.renesans.application.model.discussion;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class DiscussionQuestion {
    private QuestionId id;
    private MultilingualText title;
    private boolean active;
}
