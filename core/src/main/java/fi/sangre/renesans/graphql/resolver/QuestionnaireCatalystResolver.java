package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireCatalystResolver implements GraphQLResolver<QuestionnaireCatalystOutput> {
    private final ResolverHelper resolverHelper;
    private final MultilingualTextResolver multilingualTextResolver;

    @NonNull
    public String getTitle(@NonNull final QuestionnaireCatalystOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getDescription(@NonNull final QuestionnaireCatalystOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getOptionalText(output.getDescriptions(), resolverHelper.getLanguageCode(environment));
    }
}
