package fi.sangre.renesans.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Used in the {@link fi.sangre.renesans.repository.AnswerRepository}. Order of the fields matters
 */
@AllArgsConstructor
@Data
@Builder
public class StatisticsAnswer {
    private Long questionId;
    private Double avg;
    private Integer min;
    private Integer max;
    private Long count;

}
