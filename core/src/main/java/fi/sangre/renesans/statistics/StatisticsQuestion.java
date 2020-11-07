package fi.sangre.renesans.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsQuestion {
    private Long titleId;
    private Long catalystId;
    private StatisticsAnswer answer;
}
