package fi.sangre.renesans.graphql.assemble.media;

import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.graphql.output.media.MediaDetailsOutput;
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
    public MediaDetailsOutput from(@Nullable final MediaDetails model) {
        return Optional.ofNullable(model)
                .map(MediaDetails::getKey)
                .map(key -> MediaDetailsOutput.builder()
                        .key(key)
                        .build())
                .orElse(null);
    }
}
