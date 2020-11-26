package fi.sangre.renesans.graphql.output;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireOutput {
    private UUID id;
    private List<QuestionnaireCatalystOutput> catalysts;
    boolean finished;
}
