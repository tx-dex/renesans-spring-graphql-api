package fi.sangre.renesans.graphql.output.dialogue;

import fi.sangre.renesans.application.model.SurveyId;
import lombok.*;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueTopicOutput {
    private UUID id;
    private String title;
    private Integer questionsCount;
    private boolean active;

    // TODO: implement in the model layer
    private String image = "";

    private Collection<DialogueQuestionOutput> questions;
    private Collection<DialogueTipOutput> tips;
}
