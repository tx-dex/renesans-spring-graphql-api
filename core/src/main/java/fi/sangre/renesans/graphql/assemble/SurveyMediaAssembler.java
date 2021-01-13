package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.media.Media;
import fi.sangre.renesans.graphql.output.media.SurveyMediaOutput;
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
public class SurveyMediaAssembler {
    @NonNull
    public List<SurveyMediaOutput> from(@Nullable final List<Media> model) {
        return Optional.ofNullable(model)
                .orElse(ImmutableList.of())
                .stream()
                .map(media -> SurveyMediaOutput.builder()
                        .id(media.getId().getValue())
                        .titles(media.getTitle().getPhrases())
                        .key(media.getDetails().getKey())
                        .type(media.getType())
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
