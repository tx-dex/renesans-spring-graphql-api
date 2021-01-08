package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.graphql.facade.AfterGameFacade;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatistics;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AppQueries implements GraphQLQueryResolver {
    private final QuestionnaireFacade questionnaireFacade;
    private final AfterGameFacade afterGameFacade;
    private final ResolverHelper resolverHelper;

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'READ')")
    public QuestionnaireOutput questionnaire(@NonNull final UUID id, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting questionnaire(id={})", id);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.getQuestionnaire(id,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameCatalystStatistics> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game overview statistics questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameOverviewCatalystsStatistics(questionnaireId,
                resolverHelper.getRequiredPrincipal(environment));
    }
}
