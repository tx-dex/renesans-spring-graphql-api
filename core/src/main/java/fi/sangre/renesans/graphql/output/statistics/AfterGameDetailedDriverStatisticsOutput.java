package fi.sangre.renesans.graphql.output.statistics;

import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.graphql.output.CatalystOutput;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameDetailedDriverStatisticsOutput {
    private UUID id;
    private Map<String, String> titles;
    private CatalystOutput catalyst;
    private Collection<AfterGameQuestionStatisticsOutput> questionsStatistics;
    private Double result;
    private Double rate;
}
