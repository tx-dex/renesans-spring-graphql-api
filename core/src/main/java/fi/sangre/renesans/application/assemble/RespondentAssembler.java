package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.respondent.RespondentState;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentAssembler {

    @NonNull
    public List<Respondent> from(@NonNull final List<SurveyRespondent> respondents) {
        return respondents.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Respondent from(@NonNull final SurveyRespondent respondent) {
        return Respondent.builder()
                .id(new RespondentId(respondent.getId()))
                .email(respondent.getEmail())
                .state(RespondentState.INVITING)
                .parameterAnswers(ImmutableSet.of())
                .build();
    }
}
