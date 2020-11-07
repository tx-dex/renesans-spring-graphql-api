package fi.sangre.renesans.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal class for calculating drivers statistics
 */
@Data
@Builder
public class StatisticsGroup {
    @Builder.Default
    private Map<Long, StatisticsQuestion> questionStatistics = new HashMap<>();

    @Builder.Default
    private Double max = 0.0;

    @Builder.Default
    private Double sum = 0.0;

    public Double getPercentage() {
        return max > 0 ? sum / max : 0;
    }
}
