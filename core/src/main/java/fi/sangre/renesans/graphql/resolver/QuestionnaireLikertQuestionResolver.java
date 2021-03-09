package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.service.TranslationService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import static fi.sangre.renesans.service.TranslationService.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireLikertQuestionResolver implements GraphQLResolver<QuestionnaireLikertQuestionOutput> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;
    private final TranslationService translationService;
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public String getId(@NonNull final QuestionnaireLikertQuestionOutput output) {
        return output.getId().getValue().toString();
    }

    @NonNull
    public String getTitle(@NonNull final QuestionnaireLikertQuestionOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getSubTitle(@NonNull final QuestionnaireLikertQuestionOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getOptionalText(output.getSubTitles(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public String getLowEndLabel(@NonNull final QuestionnaireLikertQuestionOutput output, @NonNull final DataFetchingEnvironment environment) {
        final MultilingualText defaults = translationService.getPhrases(QUESTIONS_TRANSLATION_GROUP, QUESTIONS_LOW_LABEL_TRANSLATION_KEY);
        final MultilingualText combined = multilingualUtils.combine(defaults, output.getLowEndLabels());

        return multilingualTextResolver.getRequiredText(combined, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public String getHighEndLabel(@NonNull final QuestionnaireLikertQuestionOutput output, @NonNull final DataFetchingEnvironment environment) {
        final MultilingualText defaults = translationService.getPhrases(QUESTIONS_TRANSLATION_GROUP, QUESTIONS_HIGH_LABEL_TRANSLATION_KEY);
        final MultilingualText combined = multilingualUtils.combine(defaults, output.getHighEndLabels());

        return multilingualTextResolver.getRequiredText(combined, resolverHelper.getLanguageCode(environment));
    }
}
