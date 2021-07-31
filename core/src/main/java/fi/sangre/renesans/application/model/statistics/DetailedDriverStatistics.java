package fi.sangre.renesans.application.model.statistics;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(of = "id")
public class DetailedDriverStatistics {
    public static final DetailedDriverStatistics EMPTY = DetailedDriverStatistics.builder()
            .build();

    private DriverId id;
    private Catalyst catalyst;
    private Map<QuestionId, QuestionStatistics> questionsStatistics;
    private Map<String, String> titles;
    private Double result;
}
