package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class SurveyOpenQuestionStatisticsOutput {
    private UUID id;
    private String title;
    private List<String> answers;
}
