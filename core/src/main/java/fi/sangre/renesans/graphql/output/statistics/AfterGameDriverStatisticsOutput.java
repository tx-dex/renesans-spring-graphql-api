package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameDriverStatisticsOutput {
    private UUID id;
    private Map<String, String> titles;
    private Double respondentResult;
    private Double respondentGroupResult;
    private Double rate;
}
