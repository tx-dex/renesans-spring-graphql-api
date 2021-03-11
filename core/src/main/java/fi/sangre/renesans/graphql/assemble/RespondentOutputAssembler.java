package fi.sangre.renesans.graphql.assemble;

import com.sangre.mail.dto.MailStatus;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.RespondentEmail;
import fi.sangre.renesans.application.model.respondent.RespondentState;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.output.parameter.RespondentParameterAnswerOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentOutputAssembler {
    @NonNull
    public Stream<RespondentOutput> from(@NonNull final Stream<Respondent> respondents,
                                         @NonNull final Map<ParameterId, String> parameters,
                                         @NonNull final Future<Map<RespondentEmail, MailStatus>> invitations) throws InterruptedException, ExecutionException {
        log.debug("Assembling respondents outputs");
        return from(respondents, parameters, invitations.get());
    }

    @NonNull
    private Stream<RespondentOutput> from(@NonNull final Stream<Respondent> respondents, @NonNull final Map<ParameterId, String> parameters, @NonNull final Map<RespondentEmail, MailStatus> invitations) {
        return respondents
                .map(e -> from(e, parameters, invitations));
    }

    @NonNull
    private RespondentOutput from(@NonNull final Respondent respondent,
                                  @NonNull final Map<ParameterId, String> parameters,
                                  @NonNull final Map<RespondentEmail, MailStatus> invitations) {
        return RespondentOutput.builder()
                .id(respondent.getId())
                .email(respondent.getEmail())
                .parameterAnswers(respondent.getParameterAnswers().stream()
                        .map(answer -> RespondentParameterAnswerOutput.builder()
                                .rootId(answer.getRootId().getParameterId())
                                .response(parameters.get(answer.getResponse()))
                                .build())
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .state(from(respondent.getState(), invitations.get(new RespondentEmail(respondent.getEmail()))))
                .build();
    }

    @NonNull
    private RespondentState from(@NonNull RespondentState respondentState, @Nullable final MailStatus invitationsState) {
        if (invitationsState == null) {
            return respondentState;
        } else {
            if (RespondentState.INVITING.equals(respondentState)) {
                switch (invitationsState) {
                    case SENT:
                    case DELIVERED:
                    case CLICKED:
                    case OPENED:
                        return RespondentState.INVITED;
                    case SENDING:
                        return RespondentState.INVITING;
                    case SPAM:
                        return RespondentState.SPAM_EMAIL;
                    case BLOCKED:
                    case ERROR:
                    default:
                        return RespondentState.ERROR;
                }
            } else {
                return respondentState;
            }
        }
    }
}
