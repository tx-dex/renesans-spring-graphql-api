package fi.sangre.renesans.graphql.output.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AnswerDistributionOutput {
    private String label;
    private Long count;
}
