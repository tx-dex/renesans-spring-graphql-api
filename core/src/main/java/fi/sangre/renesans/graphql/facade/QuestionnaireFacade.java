package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.LikertAnswerAssembler;
import fi.sangre.renesans.application.assemble.OpenAnswerAssembler;
import fi.sangre.renesans.application.assemble.ParameterAssembler;
import fi.sangre.renesans.application.event.QuestionnaireOpenedEvent;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.input.answer.CatalystOpenQuestionAnswerInput;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionRateInput;
import fi.sangre.renesans.graphql.input.answer.ParameterAnswerInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;
    private final LikertAnswerAssembler likertAnswerAssembler;
    private final OpenAnswerAssembler openAnswerAssembler;
    private final ParameterAssembler parameterAssembler;
    private final AnswerService answerService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final RespondentId respondentId, @NonNull final String invitationHash) {
        final AuthorizationOutput output = AuthorizationOutput.builder()
                // TODO: provide invitation hash for the user so it can refresh token
                .token(tokenService.generateQuestionnaireToken(respondentId, invitationHash))
                .build();

        applicationEventPublisher.publishEvent(new QuestionnaireOpenedEvent(respondentId));

        return output;
    }

    @NonNull
    public QuestionnaireOutput getQuestionnaire(@NonNull final UUID id, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            return getQuestionnaire((RespondentPrincipal) principal);
        } else if (principal instanceof UserPrincipal) {
            return questionnaireAssembler.from(
                    organizationSurveyService.getSurvey(id));
        } else {
            throw new SurveyException("Invalid principal");
        }
    }

    @NonNull
    private QuestionnaireOutput getQuestionnaire(@NonNull final RespondentPrincipal respondent) {
        try {
            return questionnaireAssembler.from(respondent.getId(), respondent.getSurveyId());
        } catch (final InterruptedException | ExecutionException ex) {
            log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
            throw new InternalServiceException("Cannot get questionnaire");
        }
    }

    @NonNull
    public QuestionnaireOutput answerLikertQuestion(@NonNull final LikertQuestionAnswerInput answer, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());

                answerService.answerQuestion(likertAnswerAssembler.from(answer, survey), respondent.getSurveyId(), respondent.getId());

                return questionnaireAssembler.from(respondent.getId(), survey);
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

    @NonNull
    public QuestionnaireOutput rateLikertQuestion(@NonNull final LikertQuestionRateInput rate, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());

                answerService.rateQuestion(likertAnswerAssembler.from(rate, survey), respondent.getSurveyId(), respondent.getId());

                return questionnaireAssembler.from(respondent.getId(), survey);
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

    @NonNull
    public QuestionnaireOutput answerCatalystQuestion(@NonNull final CatalystOpenQuestionAnswerInput answer, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());

                answerService.answerQuestion(openAnswerAssembler.from(answer, survey), respondent.getSurveyId(), respondent.getId());

                return questionnaireAssembler.from(respondent.getId(), survey);
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

    @NonNull
    public QuestionnaireOutput answerParameter(@NonNull final ParameterAnswerInput input, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());

                final Parameter answer = parameterAssembler.fromInput(input, survey);
                answerService.answerParameter(answer, respondent.getSurveyId(), respondent.getId());

                return questionnaireAssembler.from(respondent.getId(), survey);
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

}
