package fi.sangre.renesans.persistence.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import fi.sangre.renesans.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyAssembler {
    private static final StaticTextGroup EMPTY_GROUP = StaticTextGroup.builder()
            .texts(ImmutableMap.of())
            .build();


    private final CatalystMetadataAssembler catalystMetadataAssembler;
    private final ParameterMetadataAssembler parameterMetadataAssembler;
    private final MediaMetadataAssembler mediaMetadataAssembler;
    private final TranslationMetadataAssembler translationMetadataAssembler;
    private final DiscussionQuestionMetadataAssembler discussionQuestionMetadataAssembler;

    @NonNull
    public Survey from(@NonNull final OrganizationSurvey model) {
        return from(Survey.builder()
                        .id(model.getId())
                        .archived(model.isDeleted())
                        .isDefault(false)
                        .build(),
                model);
    }

    @NonNull
    public Survey from(@NonNull final Survey entity, @NonNull final OrganizationSurvey model) {
        if (model.getVersion() != null && !model.getVersion().equals(entity.getVersion())) {
            throw new SurveyException("Survey was updated by someone else. Refresh survey to get the latest version");
        }

        entity.setVersion(model.getVersion());
        entity.setState(model.getState());

        final StaticTextGroup questions = Optional.ofNullable(model.getStaticTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_TRANSLATION_GROUP))
                .orElse(EMPTY_GROUP);

        entity.setMetadata(SurveyMetadata.builder()
                .titles(model.getTitles().getPhrases())
                .descriptions(model.getDescriptions().getPhrases())
                .logo(Optional.ofNullable(model.getLogo())
                        .map(MediaDetails::getKey)
                        .map(key -> ImageMetadata.builder()
                                .key(key)
                                .build())
                        .orElse(null))
                .hideCatalystThemePages(Boolean.TRUE.equals(model.getHideCatalystThemePages()))
                .media(mediaMetadataAssembler.from(model.getMedia()))
                .catalysts(catalystMetadataAssembler.from(model.getCatalysts(), questions))
                .parameters(parameterMetadataAssembler.from(model.getParameters()))
                .translations(translationMetadataAssembler.from(model.getStaticTexts()))
                .discussionQuestions(discussionQuestionMetadataAssembler.from(model.getDiscussionQuestions()))
                .build());

        return entity;
    }
}
