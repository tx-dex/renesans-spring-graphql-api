package fi.sangre.renesans.graphql.resolver.parameter;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.parameter.SurveyParameterValueOutput;
import fi.sangre.renesans.graphql.resolver.MetadataLanguageHelper;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyParameterValueOutputResolver implements GraphQLResolver<SurveyParameterValueOutput> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getLabel(@NonNull final SurveyParameterValueOutput output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getLabels(), resolverHelper.getLanguageCode(environment));
    }
}
