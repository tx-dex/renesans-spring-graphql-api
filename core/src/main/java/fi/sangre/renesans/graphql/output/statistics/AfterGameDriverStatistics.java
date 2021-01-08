package fi.sangre.renesans.graphql.output.statistics;

import fi.sangre.renesans.graphql.output.QuestionnaireDriverOutput;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameDriverStatistics {
    private QuestionnaireDriverOutput driver;
    private Double respondentRate;
    private Double respondentGroupRate;
}
