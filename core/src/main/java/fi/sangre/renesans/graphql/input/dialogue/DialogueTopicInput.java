package fi.sangre.renesans.graphql.input.dialogue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
@ToString
public class DialogueTopicInput {
    private UUID id;
    private String title;
    private boolean active;
    private String image;
    private List<DialogueTipInput> tips;
    private List<DialogueQuestionInput> questions;
}
