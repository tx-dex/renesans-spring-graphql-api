package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.graphql.assemble.RespondentOutputAssembler;
import fi.sangre.renesans.graphql.input.FilterInput;
import fi.sangre.renesans.graphql.input.RespondentInvitationInput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyRespondentsFacade {
    private static final String EMPTY = "";
    private final OrganizationSurveyService organizationSurveyService;
    private final AnswerService answerService;
    private final RespondentOutputAssembler respondentOutputAssembler;
    private final ParameterUtils parameterUtils;

    @NonNull
    public Collection<RespondentOutput> getSurveyRespondents(@NonNull final UUID surveyId,
                                                             @Nullable final List<FilterInput> filters,
                                                             @NonNull final String languageCode) {
        final SurveyId id = new SurveyId(surveyId);
        try {
            final OrganizationSurvey survey = organizationSurveyService.getSurvey(surveyId);
            final List<ParameterChild> allChildren = parameterUtils.getChildren(survey.getParameters());
            final Map<ParameterId, String> parameters = LazyMap.lazyMap(new HashMap<>(), parameterId -> allChildren.stream()
                    .filter(e -> parameterId.equals(e.getId()))
                    .findFirst()
                    .map(e -> MultilingualUtils.getText(e.getLabel().getPhrases(), languageCode))
                    .orElse(EMPTY));

            // TODO: combine with mail Service data
            final Future<Collection<Respondent>> answers;
            if (filters == null || filters.isEmpty()) {
                answers = answerService.getRespondentsParametersAnswersAsync(id);

                return Stream.concat(
                        organizationSurveyService.getAllRespondents(id).stream(),
                        answers.get().stream())
                        .collect(toMap(Respondent::getId, e -> e, (e1, e2) -> e2))
                        .values().stream()
                        .map(e -> respondentOutputAssembler.from(e, parameters))
                        .sorted((e1, e2) -> StringUtils.compareIgnoreCase(e1.getEmail(), e2.getEmail()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            } else {
                answers = answerService.getRespondentsParametersAnswersAsync(id); //TODO: filter
                return answers.get().stream()
                        .map(e -> respondentOutputAssembler.from(e, parameters))
                        .sorted((e1, e2) -> StringUtils.compareIgnoreCase(e1.getEmail(), e2.getEmail()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            }
        } catch (final ExecutionException | InterruptedException ex) {
            log.warn("Cannot get respondents for survey(id={})", surveyId);
            throw new InternalServiceException("Internal Server Error. Cannot get respondent list");
        }
    }

    @NonNull
    public Collection<RespondentOutput> inviteRespondents(@NonNull final UUID surveyId,
                                                          @NonNull final List<RespondentInvitationInput> invitations,
                                                          @Nullable final List<FilterInput> filters,
                                                          @NonNull final String languageCode) {
        organizationSurveyService.inviteRespondents(surveyId, invitations.stream()
                .map(e -> Invitation.builder()
                        .email(e.getEmail())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)));

        return getSurveyRespondents(surveyId, filters, languageCode);
    }
}