package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;
    private final TokenService tokenService;

    @NonNull
    public QuestionnaireOutput getQuestionnaire(@NonNull final UUID id, @NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            return questionnaireAssembler.from(
                    organizationSurveyService.getRespondent(respondent.getId()));
        } else if (principal instanceof UserPrincipal) {
            return questionnaireAssembler.from(
                    organizationSurveyService.getSurvey(id));
        } else {
            throw new SurveyException("Invalid principal");
        }
    }

    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final UUID id, @NonNull final String invitationHash) {
        return AuthorizationOutput.builder()
                .token(tokenService.generateQuestionnaireToken(id, invitationHash))
                .build();
    }
}
