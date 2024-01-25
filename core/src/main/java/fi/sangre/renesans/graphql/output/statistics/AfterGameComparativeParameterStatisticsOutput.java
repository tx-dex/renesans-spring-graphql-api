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
public class AfterGameComparativeParameterStatisticsOutput {
    private String topic;
    private String type;
    private double totalResult;
    private double totalImportance;
    private List<ParameterStatisticOutput> parameters;
}
