package fi.sangre.renesans.graphql.resolver.question;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.output.question.QuestionDriverWeights;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.service.TranslationService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static fi.sangre.renesans.service.TranslationService.*;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class LikertQuestionResolver implements GraphQLResolver<LikertQuestion> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final MultilingualUtils multilingualUtils;
    private final ResolverHelper resolverHelper;
    private final TranslationService translationService;

    @NonNull
    public String getId(@NonNull final LikertQuestion output) {
        return output.getId().asString();
    }

    @NonNull
    public String getTitle(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getSubTitle(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getOptionalText(output.getSubTitles(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public String getLowEndLabel(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        final MultilingualText defaults = translationService.getPhrases(QUESTIONS_TRANSLATION_GROUP, QUESTIONS_LOW_LABEL_TRANSLATION_KEY);
        final MultilingualText combined = multilingualUtils.combine(defaults, output.getLowEndLabels());

        return multilingualTextResolver.getRequiredText(combined, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public String getHighEndLabel(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        final MultilingualText defaults = translationService.getPhrases(QUESTIONS_TRANSLATION_GROUP, QUESTIONS_HIGH_LABEL_TRANSLATION_KEY);
        final MultilingualText combined = multilingualUtils.combine(defaults, output.getHighEndLabels());

        return multilingualTextResolver.getRequiredText(combined, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public QuestionDriverWeights getWeights(@NonNull final LikertQuestion output) {
        return new QuestionDriverWeights(Optional.ofNullable(output.getWeights())
                .orElse(ImmutableMap.of()).entrySet().stream()
                .collect(collectingAndThen(toMap(
                        e -> e.getKey().asString(),
                        Map.Entry::getValue
                ), Collections::unmodifiableMap)));
    }
}
