package fi.sangre.renesans.application.model.statistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.model.Respondent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurveyStatistics {
    private Map<String, String> titles;
    @Builder.Default
    private Long respondentCount = 0L;
    @Builder.Default
    private Double totalResult = 0.0;
    @Builder.Default
    private Map<CatalystId, CatalystStatistics> catalysts = Maps.newHashMap();
    @Builder.Default
    private List<Respondent> respondents = Lists.newArrayList();

}
