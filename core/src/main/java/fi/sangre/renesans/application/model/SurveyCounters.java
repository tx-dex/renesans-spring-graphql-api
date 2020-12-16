package fi.sangre.renesans.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static fi.sangre.renesans.application.model.Counts.ZERO_L;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurveyCounters {
    private static final SurveyCounters EMPTY = SurveyCounters.builder()
            .all(ZERO_L)
            .build();

    @Builder.Default
    private Long all = ZERO_L;

    public static SurveyCounters empty() {
        return EMPTY;
    }
}
