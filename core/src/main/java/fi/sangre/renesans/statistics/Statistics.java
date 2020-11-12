package fi.sangre.renesans.statistics;

import com.google.common.collect.Lists;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.persistence.model.Survey;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Statistics {
    private Survey survey;

    private String customerName;

    private String companyName;

    private String name;

    @Builder.Default
    private Double totalGrowthIndex = 0.0;

    private String surveyTitle;

    @Builder.Default
    private Long respondentCount = 0L;

    @Builder.Default
    private List<StatisticsCatalyst> catalysts = new ArrayList<>();

    @Builder.Default
    private List<Respondent> respondents = Lists.newArrayList();

    private StatisticsQuestionsRank questionsRank;
}
