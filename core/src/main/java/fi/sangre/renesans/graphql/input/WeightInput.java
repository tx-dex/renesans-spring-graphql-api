package fi.sangre.renesans.graphql.input;

import lombok.Data;

@Data
public class WeightInput {
    private Long questionId;
    private Long questionGroupId;
    private Double  weight;
}
