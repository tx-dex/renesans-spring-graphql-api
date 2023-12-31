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
public class RespondentCounters {
    private static final RespondentCounters EMPTY = RespondentCounters.builder()
            .invited(ZERO_L)
            .answered(ZERO_L)
            .build();

    @Builder.Default
    private Long invited = ZERO_L;
    @Builder.Default
    private Long answered = ZERO_L;

    public static RespondentCounters empty() {
        return EMPTY;
    }
}
