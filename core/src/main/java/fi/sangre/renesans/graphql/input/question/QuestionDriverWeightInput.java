package fi.sangre.renesans.graphql.input.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionDriverWeightInput {
    private Long driverId;
    private Double weight;
}
