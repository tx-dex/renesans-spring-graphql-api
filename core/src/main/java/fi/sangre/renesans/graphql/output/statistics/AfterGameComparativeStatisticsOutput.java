package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameComparativeStatisticsOutput {
    private String topic;
    private String type;
    private Double totalResult;
    private Double totalImportance;
    private List<ParameterStatisticOutput> parameters;
}
