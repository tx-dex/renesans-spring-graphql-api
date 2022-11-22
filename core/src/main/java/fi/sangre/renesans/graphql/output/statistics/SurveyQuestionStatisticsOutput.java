package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class SurveyQuestionStatisticsOutput {
    private UUID id;
    private String title;
    private Double result;
    private Double rate;
    private Long skipped;
    private Long participants;
    private String catalystTitle;
}
