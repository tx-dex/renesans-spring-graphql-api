package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswer;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AppMutations implements GraphQLMutationResolver {
    private final QuestionnaireFacade questionnaireFacade;
    private final ResolverHelper resolverHelper;
    // NOTICE!!!
    // this is public and respondent does not have token for that yet. Do not authorize it!!!
    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final UUID id, @NonNull final String invitationHash) {
        return questionnaireFacade.openQuestionnaire(id, invitationHash);
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT')")
    public QuestionnaireOutput answerOrSkipLikertQuestion(@NonNull final LikertQuestionAnswerInput answer,
                                                          @Nullable final String languageCode,
                                                          @NonNull final DataFetchingEnvironment environment) {
        log.debug("Answering question: {}", answer);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.answerLikertQuestion(answer, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT')")
    public QuestionnaireOutput answerOrSkipCatalystQuestion(@NonNull final CatalystOpenQuestionAnswer answer,
                                                            @Nullable final String languageCode,
                                                            @NonNull final DataFetchingEnvironment environment) {
        log.debug("Answering question: {}", answer);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.answerCatalystQuestion(answer, resolverHelper.getRequiredPrincipal(environment));
    }
}
