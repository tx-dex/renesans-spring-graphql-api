package fi.sangre.renesans.application.model.statistics;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurveyResult {
    public static final SurveyResult EMPTY = SurveyResult.builder()
            .statistics(null)
            .respondentIds(ImmutableSet.of())
            .build();

    private SurveyStatistics statistics;
    private Set<RespondentId> respondentIds;
}
