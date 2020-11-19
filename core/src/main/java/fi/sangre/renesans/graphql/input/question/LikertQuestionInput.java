package fi.sangre.renesans.graphql.input.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LikertQuestionInput {
    private UUID id;
    private List<QuestionDriverWeightInput> driverWeights;
}
