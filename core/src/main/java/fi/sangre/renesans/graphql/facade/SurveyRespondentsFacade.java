package fi.sangre.renesans.graphql.facade;

import com.google.common.collect.ImmutableList;
import com.sangre.mail.dto.MailStatus;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.InvitationAssembler;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.RespondentOutputAssembler;
import fi.sangre.renesans.graphql.input.RespondentInvitationInput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.InvitationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
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

    private final SurveyDao surveyDao;
    private final OrganizationSurveyService organizationSurveyService;
    private final AnswerService answerService;
    private final RespondentDao respondentDao;
    private final RespondentOutputAssembler respondentOutputAssembler;
    private final ParameterUtils parameterUtils;
    private final InvitationService invitationService;
    private final InvitationAssembler invitationAssembler;

    @NonNull
    public Collection<RespondentOutput> getSurveyRespondents(@NonNull final SurveyId surveyId,
                                                             @NonNull final List<RespondentFilter> filters,
                                                             @NonNull final String languageCode) {
        try {
            final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);
            final Future<Collection<Respondent>> answers;
            final Future<Map<RespondentEmail, MailStatus>> invitations = invitationService.getInvitationStatuses(surveyId);
            final List<ParameterChild> allChildren = parameterUtils.getChildren(survey.getParameters());
            final Map<ParameterId, String> parameters = LazyMap.lazyMap(new HashMap<>(), parameterId -> allChildren.stream()
                    .filter(e -> parameterId.equals(e.getId()))
                    .findFirst()
                    .map(e -> MultilingualUtils.getText(e.getLabel().getPhrases(), languageCode))
                    .orElse(EMPTY));

            if (filters.isEmpty()) {
                answers = answerService.getRespondentsParametersAnswersAsync(surveyId);

                return respondentOutputAssembler.from(Stream.concat(
                        organizationSurveyService.getAllRespondents(surveyId).stream(),
                        answers.get().stream())
                                .collect(toMap(Respondent::getId, e -> e, (e1, e2) -> e2))
                                .values().stream(),
                        parameters,
                        invitations)
                        .sorted((e1, e2) -> StringUtils.compareIgnoreCase(e1.getEmail(), e2.getEmail()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            } else {
                answers = answerService.getRespondentsParametersAnswersAsync(surveyId, filters);
                return respondentOutputAssembler.from( answers.get().stream(), parameters, invitations)
                        .sorted((e1, e2) -> StringUtils.compareIgnoreCase(e1.getEmail(), e2.getEmail()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            }
        } catch (final ExecutionException | InterruptedException ex) {
            log.error("Cannot get respondents for survey " + surveyId, ex);
            throw new InternalServiceException("Cannot get respondent list");
        }
    }

    @NonNull
    public Collection<RespondentOutput> inviteRespondents(@NonNull final SurveyId surveyId,
                                                          @NonNull final RespondentInvitationInput invitation,
                                                          @NonNull final List<RespondentFilter> filters,
                                                          @NonNull final String languageCode,
                                                          @NonNull final UserPrincipal principal) {
        organizationSurveyService.inviteRespondents(surveyId,
                invitationAssembler.from(invitation),
                Pair.of(principal.getName(), principal.getEmail()));

        return getSurveyRespondents(surveyId, filters, languageCode);
    }

    @NonNull
    public RespondentOutput softDeleteRespondent(@NonNull final RespondentId id) {
        try {
            final Respondent respondent = respondentDao.softDeleteRespondent(id);

            return RespondentOutput.builder()
                    .id(id)
                    .email(respondent.getEmail())
                    .state(respondent.getState())
                    .parameterAnswers(ImmutableList.of())
                    .build();
        } catch (final Exception ex) {
            log.warn("Cannot soft delete respondent(id={})", id, ex);
            throw new SurveyException("Internal Server Error. Cannot delete respondent");
        }
    }
}