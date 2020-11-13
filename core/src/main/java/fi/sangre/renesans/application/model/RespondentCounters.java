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
    @Builder.Default
    private Long invited = ZERO_L;
    @Builder.Default
    private Long answered = ZERO_L;
}
