package fi.sangre.renesans.graphql.resolver.question;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
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
public class OpenQuestionResolver implements GraphQLResolver<OpenQuestion> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getId(@NonNull final OpenQuestion output) {
        return output.getId().asString();
    }

    @NonNull
    public String getTitle(@NonNull final OpenQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }
}
