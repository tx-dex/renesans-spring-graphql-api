package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
                .texts(new MultilingualText(ImmutableMap.of(languageTag, input.getText())))
                .build();
    }

    @NonNull
    public List<StaticText> fromMetadata(@Nullable final Map<String, Map<String, String>> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableMap.of())
                .entrySet().stream()
                .map(e -> StaticText.builder()
                        .id(e.getKey())
                        .texts(new MultilingualText(e.getValue()))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


}
