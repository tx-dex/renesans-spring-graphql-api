package fi.sangre.renesans.graphql.resolver.parameter;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireTreeParameterOutput;
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
public class QuestionnaireTreeParameterResolver implements GraphQLResolver<QuestionnaireTreeParameterOutput> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getLabel(@NonNull final QuestionnaireTreeParameterOutput output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getLabels(), resolverHelper.getLanguageCode(environment));
    }
}

