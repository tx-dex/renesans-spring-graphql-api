package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireTranslationOutput;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireResolver implements GraphQLResolver<QuestionnaireOutput> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    public QuestionnaireTranslationOutput getStaticTexts(@NonNull final QuestionnaireOutput output, @NonNull final DataFetchingEnvironment environment) {
        final String languageTag = resolverHelper.getLanguageCode(environment);

        final Map<String, Map<String, String>> translations = output.getStaticTexts().stream()
                .collect(collectingAndThen(toMap(
                        StaticTextGroup::getId,
                        group -> group.getTexts().stream()
                                .collect(collectingAndThen(toMap(
                                        StaticText::getId,
                                        text -> metadataLanguageHelper.getOptionalText(text.getTexts().getPhrases(), languageTag)), Collections::unmodifiableMap)))
                        , Collections::unmodifiableMap));

        return new QuestionnaireTranslationOutput(translations);
    }
}
