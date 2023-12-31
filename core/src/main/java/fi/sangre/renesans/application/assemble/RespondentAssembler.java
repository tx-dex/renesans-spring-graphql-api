package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.respondent.RespondentState;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentAssembler {
    private final ParameterAnswerAssembler parameterAnswerAssembler;

    @NonNull
    public List<Respondent> from(@NonNull final Map<SurveyRespondent, List<ParameterAnswerEntity>> answers) {
        return answers.entrySet().stream()
                .map(e -> from(e.getKey(), parameterAnswerAssembler.fromRespondentAnswers(e.getValue())))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<Respondent> from(@NonNull final List<SurveyRespondent> respondents) {
        return respondents.stream()
                .map(e -> from(e, ImmutableList.of()))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public Respondent from(@NonNull final SurveyRespondent respondent) {
        return from(respondent, ImmutableList.of());
    }

    @NonNull
    private Respondent from(@NonNull final SurveyRespondent respondent, @NonNull final List<ParameterItemAnswer> answers) {
        return Respondent.builder()
                .id(new RespondentId(respondent.getId()))
                .surveyId(new SurveyId(respondent.getSurveyId()))
                .email(respondent.getEmail())
                .state(from(respondent.getState()))
                .parameterAnswers(answers)
                .build();
    }

    @NonNull
    private RespondentState from(@NonNull final SurveyRespondentState respondentState) {
        switch (respondentState) {
            case INVITING:
                return RespondentState.INVITING;
            case OPENED:
                return RespondentState.OPENED;
            case ANSWERING_PARAMETERS:
                return RespondentState.ANSWERING_PARAMETERS;
            case ANSWERED_PARAMETERS:
                return RespondentState.ANSWERED_PARAMETERS;
            case OPENED_QUESTIONS:
                return RespondentState.OPENED_QUESTIONS;
            case ANSWERING:
                return RespondentState.ANSWERING;
            case ANSWERED:
                return RespondentState.ANSWERED;
            case ERROR:
                return RespondentState.ERROR;
            default:
                return RespondentState.INVITED;
        }
    }
}
