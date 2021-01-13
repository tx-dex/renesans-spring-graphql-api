package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.MediaId;
import fi.sangre.renesans.application.model.media.Media;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.application.model.media.MediaType;
import fi.sangre.renesans.application.utils.MediaUtils;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.media.SurveyMediaInput;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.MediaMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.PdfMetadata;
import fi.sangre.renesans.persistence.model.metadata.media.VideoMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class MediaAssembler {
    private final MultilingualUtils multilingualUtils;
    private final MediaUtils mediaUtils;

    @NonNull
    public List<Media> fromInputs(@NonNull final List<SurveyMediaInput> inputs, @NonNull final String languageTag) {
        return inputs.stream()
                .map(input -> from(input, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Media from(@NonNull final SurveyMediaInput input, @NonNull final String languageTag) {
        if (input.getDetails() == null) {
            throw new SurveyException("Input details must not be null");
        }

        return Media.builder()
                .id(Optional.ofNullable(input.getId())
                        .map(MediaId::new)
                        .orElse(null))
                .details(MediaDetails.builder()
                        .key(input.getDetails().getKey())
                        .build())
                .type(mediaUtils.getTypeFromKey(input.getDetails().getKey()))
                .title(multilingualUtils.create(input.getTitle(), languageTag))
                .build();
    }

    @NonNull
    public List<Media> fromMetadata(@Nullable final List<MediaMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of()).stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Media from(@NonNull final MediaMetadata metadata) {
        if (metadata instanceof ImageMetadata) {
            final ImageMetadata image = (ImageMetadata) metadata;
            return Media.builder()
                    .id(new MediaId(image.getId()))
                    .type(MediaType.IMAGE)
                    .details(MediaDetails.builder()
                            .key(image.getKey())
                            .build())
                    .title(multilingualUtils.create(image.getTitles()))
                    .build();
        } else if (metadata instanceof VideoMetadata) {
            final VideoMetadata video = (VideoMetadata) metadata;
            return Media.builder()
                    .id(new MediaId(video.getId()))
                    .type(MediaType.VIDEO)
                    .details(MediaDetails.builder()
                            .key(video.getKey())
                            .build())
                    .title(multilingualUtils.create(video.getTitles()))
                    .build();
        } else if (metadata instanceof PdfMetadata) {
            final PdfMetadata pdf = (PdfMetadata) metadata;
            return Media.builder()
                    .id(new MediaId(pdf.getId()))
                    .type(MediaType.PDF)
                    .details(MediaDetails.builder()
                            .key(pdf.getKey())
                            .build())
                    .title(multilingualUtils.create(pdf.getTitles()))
                    .build();
        } else {
            throw new SurveyException("Invalid media type");
        }
    }
}
