package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.QuestionnaireDriverOutput;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireDriverResolver implements GraphQLResolver<QuestionnaireDriverOutput> {
    private final ResolverHelper resolverHelper;
    private final MetadataLanguageHelper metadataLanguageHelper;

    @NonNull
    public String getTitle(@NonNull final QuestionnaireDriverOutput output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getTitles().getPhrases(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public String getDescription(@NonNull final QuestionnaireDriverOutput output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getDescriptions().getPhrases(), resolverHelper.getLanguageCode(environment));
    }
}
