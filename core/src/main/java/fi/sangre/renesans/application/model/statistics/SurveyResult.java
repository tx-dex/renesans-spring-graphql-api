package fi.sangre.renesans.application.model.statistics;

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
    private SurveyStatistics statistics;
    private Set<RespondentId> respondentIds;
}
