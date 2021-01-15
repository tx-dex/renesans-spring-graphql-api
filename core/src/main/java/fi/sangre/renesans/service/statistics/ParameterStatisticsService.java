package fi.sangre.renesans.service.statistics;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.filter.RespondentParameterFilter;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j

@Service
public class ParameterStatisticsService {
    private final RespondentStatisticsService respondentStatisticsService;

    @NonNull
    // TODO: cache
    public SurveyResult calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final ParameterId parameterId) {
        return respondentStatisticsService.calculateStatistics(survey, ImmutableList.of(RespondentParameterFilter.builder()
                .values(ImmutableList.of(parameterId.getValue()))
                .build()));
    }
}
