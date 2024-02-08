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
public class AfterGameQuestionStatisticsOutput {
    private UUID id;
    private Map<String, String> titles;
    private Map<String, String> catalystTitles;
    private Double result;
    private Double rate;
    private Long participants;
    private Long skipped;
}
