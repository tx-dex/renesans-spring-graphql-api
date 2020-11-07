package fi.sangre.renesans.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDriver {
    private Long id;
    private Long catalystId;
    private String pdfName;
    private String title;
    private Double result;
    private Double weighedResult;
    private Double weight;
    private Double weightModifier;
    private Double importance;
}
