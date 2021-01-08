package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.graphql.output.media.MediaDetailsOutput;
import fi.sangre.renesans.persistence.model.metadata.media.ImageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class MediaDetailsAssembler {

    @Nullable
    public MediaDetailsOutput from(@Nullable final ImageMetadata model) {
        return Optional.ofNullable(model)
                .map(ImageMetadata::getKey)
                .map(v -> MediaDetailsOutput.builder()
                        .key(v)
                        .build())
                .orElse(null);
    }
}
