package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireTranslationOutput;
import fi.sangre.renesans.service.TranslationService;
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
    private final TranslationService translationService;
    private final ResolverHelper resolverHelper;

    public QuestionnaireTranslationOutput getStaticTexts(@NonNull final QuestionnaireOutput output, @NonNull final DataFetchingEnvironment environment) {
        final String languageTag = resolverHelper.getLanguageCode(environment);
        final StaticTextGroup emptyGroup = StaticTextGroup.builder()
                .texts(ImmutableMap.of())
                .build();
        final MultilingualText emptyText = new MultilingualText(ImmutableMap.of());

        final Map<String, Map<String, String>> translations = translationService.getTranslations(languageTag).entrySet().stream()
                .collect(collectingAndThen(toMap(
                        Map.Entry::getKey,
                        group -> group.getValue().entrySet().stream()
                                .collect(collectingAndThen(toMap(
                                        Map.Entry::getKey,
                                        text -> output.getStaticTexts().getOrDefault(group.getKey(), emptyGroup)
                                                .getTexts().getOrDefault(text.getKey(), emptyText)
                                                .getPhrases().getOrDefault(languageTag, text.getValue().getText()))
                                        , Collections::unmodifiableMap)))
                        , Collections::unmodifiableMap));

        return new QuestionnaireTranslationOutput(translations);
    }
}
