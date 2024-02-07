package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.graphql.output.statistics.AnswerDistributionOutput;
import fi.sangre.renesans.graphql.output.statistics.AnswerDistributionsOutput;
import fi.sangre.renesans.persistence.model.statistics.AnswerDistribution;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j

@Component
public class AnswerDistributionAssembler {

    @NonNull
    public AnswerDistributionsOutput from(List<AnswerDistribution> resultDistributions,
                                          List<AnswerDistribution> rateDistributions) {
        return AnswerDistributionsOutput.builder()
                .result(resultDistributions.stream().map(this::from).collect(Collectors.toList()))
                .importance(rateDistributions.stream().map(this::from).collect(Collectors.toList()))
                .build();
    }

    @NonNull
    private AnswerDistributionOutput from(AnswerDistribution distribution) {
        return new AnswerDistributionOutput(distribution.getLabel(), distribution.getCount());
    }
}
