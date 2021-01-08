package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.media.MediaDetailsOutput;
import fi.sangre.renesans.graphql.output.media.SurveyMediaOutput;
import fi.sangre.renesans.service.MediaService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyMediaResolver implements GraphQLResolver<SurveyMediaOutput> { //TODO: add ImageDetails grapqQL DTO if there will be images for other entities
    private final MediaService mediaService;
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getTitle(@NonNull final SurveyMediaOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public MediaDetailsOutput getDetails(@NonNull final SurveyMediaOutput output) {
        return MediaDetailsOutput.builder()
                .key(output.getKey())
                .build();
    }

}
