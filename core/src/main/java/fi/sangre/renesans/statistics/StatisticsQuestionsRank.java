package fi.sangre.renesans.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsQuestionsRank {
    private List<StatisticsQuestion> questionStatistics;
}
