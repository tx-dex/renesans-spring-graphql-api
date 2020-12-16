package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.utils.RespondentUtils;
import fi.sangre.renesans.persistence.model.RespondentStateCounters;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentCounterAssembler {
    private final RespondentUtils respondentUtils;

    @NonNull
    public RespondentCounters fromCounters(@NonNull final List<RespondentCounters> counters) {
        return RespondentCounters.builder()
                .invited(counters.stream()
                        .mapToLong(RespondentCounters::getInvited)
                        .sum())
                .answered(counters.stream()
                        .mapToLong(RespondentCounters::getAnswered)
                        .sum()
                        )
                .build();
    }

    @NonNull
    public RespondentCounters fromRespondents(@NonNull final List<SurveyRespondent> respondents) {
        return RespondentCounters.builder()
                .invited(respondents.stream()
                        .filter(respondentUtils::isInvited)
                        .count())
                .answered(respondents.stream()
                        .filter(respondentUtils::isAnswered)
                        .count())
                .build();
    }

    @NonNull
    public RespondentCounters from(@NonNull final RespondentStateCounters counters) {
        return RespondentCounters.builder()
                .invited(counters.getAll())
                .answered(counters.getAnswered())
                .build();
    }
}
