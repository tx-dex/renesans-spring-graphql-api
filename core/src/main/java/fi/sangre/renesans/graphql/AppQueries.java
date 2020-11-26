package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
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
public class AppQueries implements GraphQLQueryResolver {
    private final QuestionnaireFacade questionnaireFacade;
    private final ResolverHelper resolverHelper;

    @NonNull
    // TODO: authorize
    @PreAuthorize("isAuthenticated()")
    public QuestionnaireOutput questionnaire(@NonNull final UUID id, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.getQuestionnaire(id, resolverHelper.getRequiredPrincipal(environment));
    }

}
