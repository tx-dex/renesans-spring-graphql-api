package fi.sangre.renesans.graphql.output.statistics;

import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameCatalystStatistics {
    private QuestionnaireCatalystOutput catalyst;
    private Double respondentRate;
    private Double respondentGroupRate;
    private Collection<AfterGameDriverStatistics> drivers;
}
