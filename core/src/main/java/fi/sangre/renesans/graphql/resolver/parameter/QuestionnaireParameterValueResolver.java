package fi.sangre.renesans.graphql.resolver.parameter;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterItemOutput;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireParameterValueResolver implements GraphQLResolver<QuestionnaireParameterItemOutput> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getLabel(@NonNull final QuestionnaireParameterItemOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getLabels(), resolverHelper.getLanguageCode(environment));
    }
}
