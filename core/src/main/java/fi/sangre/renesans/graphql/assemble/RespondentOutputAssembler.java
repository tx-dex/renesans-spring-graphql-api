package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.output.parameter.RespondentParameterAnswerOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentOutputAssembler {
    @NonNull
    public RespondentOutput from(@NonNull final Respondent respondent, @NonNull final Map<ParameterId, String> parameters) {
        return RespondentOutput.builder()
                .id(respondent.getId())
                .email(respondent.getEmail())
                .parameterAnswers(respondent.getParameterAnswers().stream()
                        .map(answer -> RespondentParameterAnswerOutput.builder()
                                .id(answer.getRootId().getParameterId())
                                .response(parameters.get(answer.getResponse()))
                                .build())
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .state(respondent.getState())
                .build();
    }
}
