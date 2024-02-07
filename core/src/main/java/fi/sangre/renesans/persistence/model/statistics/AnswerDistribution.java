package fi.sangre.renesans.persistence.model.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AnswerDistribution {
    private String label;
    private Long count;
}
