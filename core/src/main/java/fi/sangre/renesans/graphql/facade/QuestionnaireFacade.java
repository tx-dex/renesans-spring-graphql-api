package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.LikertAnswerAssembler;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswer;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final AnswerDao answerDao;
    private final TokenService tokenService;

    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final UUID id, @NonNull final String invitationHash) {
        return AuthorizationOutput.builder()
                // TODO: provide invitation hash for the user so it can refresh token
                .token(tokenService.generateQuestionnaireToken(id, invitationHash))
                .build();
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
            throw new InternalServiceException("Internal Server Error. Cannot get questionnaire");
        }
    }

    @NonNull
    public QuestionnaireOutput answerLikertQuestion(@NonNull final LikertQuestionAnswerInput answer, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());

                answerDao.answerQuestion(likertAnswerAssembler.from(answer, survey), respondent.getSurveyId(), respondent.getId());

                return questionnaireAssembler.from(respondent.getId(), survey);
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Internal Server Error. Cannot get questionnaire");
            }
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

    @NonNull
    public QuestionnaireOutput answerCatalystQuestion(@NonNull final CatalystOpenQuestionAnswer answer, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            //TODO: implement
            return getQuestionnaire((RespondentPrincipal) principal);
        } else {
            throw new SurveyException("Only respondent can answer");
        }
    }

}
