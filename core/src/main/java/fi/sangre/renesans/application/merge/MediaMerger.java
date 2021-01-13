package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.MediaId;
import fi.sangre.renesans.application.model.media.Media;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class MediaMerger {
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<Media> combine(@NonNull final List<Media> existing, @Nullable final List<Media> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing);
        } else {
            final List<Media> combined = new LinkedList<>();
            final Map<MediaId, Media> existingParameters = existing.stream()
                    .collect(collectingAndThen(toMap(Media::getId, v -> v), Collections::unmodifiableMap));

            for (final Media input : inputs) {
                combined.add(combine(existingParameters, input));
            }

            log.trace("Combined media: {}", combined);

            return Collections.unmodifiableList(combined);
        }
    }

    @NonNull
    private Media combine(@NonNull final Map<MediaId, Media> existing, @NonNull final Media input) {
        final MediaId id;
        final Media combined;
        if (input.getId() == null) {
            id = new MediaId(UUID.randomUUID());
            combined = Media.builder().id(id).build();
        } else {
            id = new MediaId(input.getId());
            combined = Optional.ofNullable(existing.get(id))
                    .orElse(Media.builder().id(id).build());
        }

        return combine(combined, input);
    }

    @NonNull
    private Media combine(@NonNull final Media existing, @NonNull final Media input) {
        if (input.getDetails() == null) {
            throw new SurveyException("Input details must not be null");
        } else if (StringUtils.trimToNull(input.getDetails().getKey()) == null) {
            throw new SurveyException("Input details key must not be null");
        }

        existing.setType(input.getType());
        existing.setTitle(multilingualUtils.combine(existing.getTitle(), input.getTitle()));
        existing.setDetails(MediaDetails.builder()
                .key(input.getDetails().getKey())
                .build());

        return existing;
    }
}
