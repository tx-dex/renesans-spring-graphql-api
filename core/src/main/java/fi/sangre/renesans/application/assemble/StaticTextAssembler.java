package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import fi.sangre.renesans.persistence.model.metadata.PhrasesGroupMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextAssembler {

    @NonNull
    public StaticText from(@NonNull final StaticTextInput input, @NonNull final String languageTag) {
        checkArgument(input.getId() != null, "input.id must be provided");

        return StaticText.builder()
                .id(input.getId())
                .texts(new MultilingualText(ImmutableMap.of(languageTag, StringUtils.trim(input.getText()))))
                .build();
    }

    @NonNull
    public List<StaticTextGroup> fromMetadata(@Nullable final Map<String, PhrasesGroupMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableMap.of())
                .entrySet().stream()
                .map(group -> StaticTextGroup.builder()
                        .id(group.getKey())
                        .title(group.getValue().getTitle())
                        .description(group.getValue().getDescription())
                        .texts(group.getValue().getPhrases().entrySet().stream()
                                .map(text -> StaticText.builder()
                                        .id(text.getKey())
                                        .title(text.getValue().getTitle())
                                        .description(text.getValue().getDescription())
                                        .html(text.getValue().isHtml())
                                        .texts(new MultilingualText(text.getValue().getPhrases()))
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
