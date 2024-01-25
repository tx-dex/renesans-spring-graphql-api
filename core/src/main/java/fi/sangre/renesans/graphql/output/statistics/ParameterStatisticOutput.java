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
public class ParameterStatisticOutput {
    private String label;
    private List<String> parents;
    private Double result;
    private Double rate;
}
