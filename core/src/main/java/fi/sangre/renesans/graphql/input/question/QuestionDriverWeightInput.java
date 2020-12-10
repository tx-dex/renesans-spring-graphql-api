package fi.sangre.renesans.graphql.input.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionDriverWeightInput {
    private UUID questionId;
    private Long driverId; //TODO: change to uuid
    private Double weight;
}
