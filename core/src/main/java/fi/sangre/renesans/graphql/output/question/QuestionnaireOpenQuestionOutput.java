package fi.sangre.renesans.graphql.output.question;

import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireOpenQuestionOutput implements QuestionnaireQuestionOutput {
    private QuestionId id;
    private Map<String, String> titles;
    @Builder.Default
    private boolean skipped = false;
    @Builder.Default
    private boolean answered = false;
    @Accessors(fluent = true)
    @Builder.Default
    private boolean isPublic = false;
    private String response;
}
