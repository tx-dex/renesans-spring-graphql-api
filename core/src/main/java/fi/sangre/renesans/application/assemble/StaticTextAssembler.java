package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import fi.sangre.renesans.persistence.model.metadata.PhrasesGroupMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextAssembler {
    private final MultilingualUtils multilingualUtils;
    @NonNull
    public MultilingualText from(@NonNull final StaticTextInput input, @NonNull final String languageTag) {
        checkArgument(input.getId() != null, "input.id must be provided");

        return multilingualUtils.create(input.getText(), languageTag);
    }

    @NonNull
    public Map<String, StaticTextGroup> fromMetadata(@Nullable final Map<String, PhrasesGroupMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableMap.of())
                .entrySet().stream()
                .collect(collectingAndThen(toMap(
                        Map.Entry::getKey,
                        group -> StaticTextGroup.builder()
                                .texts(group.getValue().getPhrases().entrySet().stream()
                                        .collect(collectingAndThen(toMap(
                                                Map.Entry::getKey,
                                                e -> multilingualUtils.create(e.getValue().getPhrases())
                                        ), Collections::unmodifiableMap)))
                                .build()), Collections::unmodifiableMap));
    }
}
