package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class SurveyQuestionStatisticsOutput {
    private String id;
    private String title;
    private Double result;
    private Double rate;
}
