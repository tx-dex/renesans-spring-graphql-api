package fi.sangre.renesans.graphql.output.statistics;

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
public class AfterGameOpenQuestionStatisticsOutput {
    private UUID questionId;
    private Map<String, String> titles;
    private Collection<OpenQuestionAnswerOutput> answers;
    private Map<String, String> catalystTitles;
}
