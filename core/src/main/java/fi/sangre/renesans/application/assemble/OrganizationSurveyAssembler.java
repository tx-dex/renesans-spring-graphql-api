package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.input.CatalystInput;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import fi.sangre.renesans.graphql.input.discussion.DiscussionQuestionInput;
import fi.sangre.renesans.graphql.input.media.MediaDetailsInput;
import fi.sangre.renesans.graphql.input.media.SurveyMediaInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyAssembler {
    private static final StaticTextGroup EMPTY_GROUP = StaticTextGroup.builder()
            .texts(ImmutableMap.of())
            .build();

    private final MediaAssembler mediaAssembler;
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;
    private final CatalystAssembler catalystAssembler;
    private final DiscussionQuestionAssembler discussionQuestionAssembler;
    private final MultilingualUtils multilingualUtils;
    private final SurveyRepository surveyRepository;

    @NonNull
    public OrganizationSurvey fromQuestionsInput(@NonNull final UUID id,
                                                 @NonNull final Long version,
                                                 @NonNull final List<CatalystInput> input,
                                                 @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .catalysts(catalystAssembler.fromInputs(input, languageTag).stream()
                        .peek(e -> {
                            // we don't want to update drivers or titles
                            e.setTitles(multilingualUtils.empty());
                            e.setDescriptions(multilingualUtils.empty());
                            e.setDrivers(null);
                        })
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromCatalystAndDriversInput(@NonNull final UUID id,
                                                          @NonNull final Long version,
                                                          @NonNull final List<CatalystInput> input,
                                                          @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .catalysts(catalystAssembler.fromInputs(input, languageTag).stream()
                        .peek(e -> {
                            // we don't want to update questions here
                            e.setQuestions(null);
                            e.setOpenQuestions(null);
                        })
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromParametersInput(@NonNull final UUID id,
                                                  @NonNull final Long version,
                                                  @NonNull final List<SurveyParameterInput> input,
                                                  @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .parameters(parameterAssembler.fromInputs(input, languageTag))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromMediasInput(@NonNull final UUID id,
                                               @NonNull final Long version,
                                               @NonNull final List<SurveyMediaInput> input,
                                               @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .media(mediaAssembler.fromInputs(input, languageTag))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromStaticTextInput(@NonNull final UUID id,
                                                  @NonNull final Long version,
                                                  @NonNull final StaticTextInput input,
                                                  @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        if (input.getText() != null) {
            return OrganizationSurvey.builder()
                    .id(id)
                    .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                    .version(version)
                    .staticTexts(ImmutableMap.of(input.getTextGroupId(), StaticTextGroup.builder()
                            .texts(ImmutableMap.of(input.getId(), staticTextAssembler.from(input, languageTag)))
                            .build()))
                    .build();
        } else {
            return OrganizationSurvey.builder()
                    .id(id)
                    .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                    .version(version)
                    .staticTexts(ImmutableMap.of())
                    .build();
        }
    }

    @NonNull
    public OrganizationSurvey fromLogoInput(@NonNull final UUID id,
                                            @NonNull final Long version,
                                            @Nullable final MediaDetailsInput input) {
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        final MediaDetails logo = Optional.ofNullable(input)
                .map(MediaDetailsInput::getKey)
                .map(StringUtils::trimToNull)
                .map(key -> MediaDetails.builder()
                        .key(key)
                        .build())
                .orElse(MediaDetails.builder()
                        .build());

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .logo(logo)
                .build();
    }

    @NonNull
    public OrganizationSurvey fromDiscussionQuestionInput(@NonNull final UUID id,
                                                          @NonNull final Long version,
                                                          @NonNull final List<DiscussionQuestionInput> input,
                                                          @NonNull final String languageTag) {
        Survey surveyEntity = surveyRepository.findById(id).orElse(null);

        return OrganizationSurvey.builder()
                .id(id)
                .isDialogueActive(surveyEntity != null ? surveyEntity.getIsDialogueActive() : false)
                .version(version)
                .discussionQuestions(discussionQuestionAssembler.fromInput(input, languageTag))
                .build();
    }

    @NonNull
    public OrganizationSurvey from(@NonNull final Survey survey) {
        final SurveyMetadata metadata = survey.getMetadata();

        final Map<String, StaticTextGroup> texts = staticTextAssembler.fromMetadata(metadata.getTranslations());
        final StaticTextGroup questionsGroup = texts.getOrDefault(TranslationService.QUESTIONS_TRANSLATION_GROUP, EMPTY_GROUP);

        return OrganizationSurvey.builder()
                .id(survey.getId())
                .isDialogueActive(survey.getIsDialogueActive())
                .version(survey.getVersion())
                .state(survey.getState())
                .logo(Optional.ofNullable(metadata.getLogo())
                        .map(ImageMetadata::getKey)
                        .map(key -> MediaDetails.builder()
                                .key(key)
                                .build())
                        .orElse(null))
                .titles(multilingualUtils.create(metadata.getTitles()))
                .descriptions(multilingualUtils.create(metadata.getDescriptions()))
                .media(mediaAssembler.fromMetadata(metadata.getMedia()))
                .catalysts(catalystAssembler.fromMetadata(metadata.getCatalysts(), questionsGroup))
                .hideCatalystThemePages(Boolean.TRUE.equals(metadata.getHideCatalystThemePages()))
                .parameters(parameterAssembler.fromMetadata(metadata.getParameters()))
                .discussionQuestions(discussionQuestionAssembler.fromMetadata(metadata.getDiscussionQuestions()))
                .staticTexts(texts)
                .build();
    }
}
