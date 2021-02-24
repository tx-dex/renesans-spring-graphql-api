package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class SurveyCatalystStatisticsOutput {
    private String id;
    private String title;
    private Double result;
    private Collection<SurveyDriverStatisticsOutput> drivers;
    private Collection<SurveyQuestionStatisticsOutput> questions;
    private Collection<SurveyOpenQuestionStatisticsOutput> openQuestions;
    @Deprecated
    private SurveyOpenQuestionStatisticsOutput openQuestion;
}
