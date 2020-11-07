package fi.sangre.renesans.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsCatalyst {
    private Long id;
    private String pdfName;
    private String title;

    @Builder.Default
    private Double index = 0.0; //TODO: remove
    @Builder.Default
    private Double weighedIndex = 0.0; //TODO: remove

    private Double result;
    private Double weighedResult;
    private Double weight;
    private Double importance;
    private List<StatisticsDriver> developmentTrackIndices;
    private List<StatisticsQuestion> questions;
}
