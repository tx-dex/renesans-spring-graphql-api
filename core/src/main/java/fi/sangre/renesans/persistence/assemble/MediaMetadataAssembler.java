package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.media.Media;
import fi.sangre.renesans.application.model.media.MediaType;
import fi.sangre.renesans.exception.SurveyException;
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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class MediaMetadataAssembler {

    @Nullable
    public List<MediaMetadata> from(@Nullable final List<Media> media) {
        if (media == null || media.isEmpty()) {
            return null;
        } else {
            return media.stream()
                    .map(this::from)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
    }

    @NonNull
    private MediaMetadata from(@NonNull final Media media) {
        if (MediaType.IMAGE.equals(media.getType())) {
            return ImageMetadata.builder()
                    .id(media.getId().getValue())
                    .key(media.getDetails().getKey())
                    .titles(media.getTitle().getPhrases())
                    .build();
        } else if (MediaType.VIDEO.equals(media.getType())) {
            return VideoMetadata.builder()
                    .id(media.getId().getValue())
                    .key(media.getDetails().getKey())
                    .titles(media.getTitle().getPhrases())
                    .build();
        } else if (MediaType.PDF.equals(media.getType())) {
            return PdfMetadata.builder()
                    .id(media.getId().getValue())
                    .key(media.getDetails().getKey())
                    .titles(media.getTitle().getPhrases())
                    .build();
        } else {
            throw new SurveyException("Invalid media type");
        }
    }
}
