package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameParameterStatisticsOutput {
    private Map<String, String> titles;
    private String value;
    private Double rate;
}
