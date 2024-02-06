package fi.sangre.renesans.persistence.model.statistics;

import fi.sangre.renesans.application.utils.StatisticsUtils;
import lombok.*;

import java.util.UUID;

/**
 * Used in the {@link fi.sangre.renesans.persistence.repository.LikerQuestionAnswerRepository}.
 * Order of the fields matters
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "questionId")
public class QuestionStatistics implements StatisticsResult {
    public static final QuestionStatistics EMPTY = QuestionStatistics.builder()
            .avg(null)
            .rate(null)
            .build();

    private UUID questionId;
    private Double avg;
    private Integer min;
    private Integer max;
    private Long count;
    private Double rate;
    private Long skipped;

    public Double getResult() {
        return StatisticsUtils.indexToRate(avg);
    }
    public Double getWeighedResult() { return getResult(); }
}
