package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AnswerDistributionsOutput {
    private List<AnswerDistributionOutput> result;
    private List<AnswerDistributionOutput> importance;
}
