package fi.sangre.renesans.graphql.resolver;


import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.SurveyTemplate;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyTemplateResolver implements GraphQLResolver<SurveyTemplate> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    @NonNull
    public Long getId(@NonNull final SurveyTemplate output) {
        return output.getId().getValue();
    }

    @NonNull
    public String getTitle(@NonNull final SurveyTemplate output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getTitles().getPhrases(),
                resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getDescription(@NonNull final SurveyTemplate output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getOptionalText(output.getDescriptions().getPhrases(),
                resolverHelper.getLanguageCode(environment));
    }
}
