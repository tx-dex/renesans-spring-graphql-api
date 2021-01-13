package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyAssembler {
    private final CatalystMetadataAssembler catalystMetadataAssembler;
    private final ParameterMetadataAssembler parameterMetadataAssembler;
    private final MediaMetadataAssembler mediaMetadataAssembler;
    private final TranslationMetadataAssembler translationMetadataAssembler;

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

        entity.setMetadata(SurveyMetadata.builder()
                .titles(model.getTitles().getPhrases())
                .descriptions(model.getDescriptions().getPhrases())
                .logo(Optional.ofNullable(model.getLogo())
                        .map(MediaDetails::getKey)
                        .map(key -> ImageMetadata.builder()
                                .key(key)
                                .build())
                        .orElse(null))
                .media(mediaMetadataAssembler.from(model.getMedia()))
                .catalysts(catalystMetadataAssembler.from(model.getCatalysts()))
                .parameters(parameterMetadataAssembler.from(model.getParameters()))
                .translations(translationMetadataAssembler.from(model.getStaticTexts()))
                .build());

        return entity;
    }
}
