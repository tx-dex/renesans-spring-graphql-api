package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AfterGameOverviewVisionAttainmentIndicatorOutput {
    private UUID id;
    private Double value;
}
