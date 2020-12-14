package fi.sangre.renesans.application.merge;


import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextMerger {
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public Map<String, StaticTextGroup> combine(@NonNull final Map<String, StaticTextGroup> existing, @Nullable final Map<String, StaticTextGroup> input) {
        if (input == null) {
            return ImmutableMap.copyOf(existing);
        } else {
            final ImmutableMap.Builder<String, StaticTextGroup> builder = ImmutableMap.builder();
            builder.putAll(existing);

            for(final Map.Entry<String, StaticTextGroup> group : input.entrySet()) {
                final StaticTextGroup found = existing.get(group.getKey());

                if (found == null) {
                    builder.put(group.getKey(), group.getValue());
                } else {
                    combineGroup(found, group.getValue());
                }


            }

            return builder.build();
        }
    }

    private void combineGroup(@NonNull final StaticTextGroup existing, @NonNull final StaticTextGroup input) {
        final Map<String, MultilingualText> createdOrUpdated = new LinkedHashMap<>();

        for (final Map.Entry<String, MultilingualText> text : input.getTexts().entrySet()) {
            final MultilingualText found = existing.getTexts().get(text.getKey());

            if (found == null) {
                createdOrUpdated.put(text.getKey(), text.getValue());
            } else {
                createdOrUpdated.put(text.getKey(), multilingualUtils.combine(found, text.getValue()));
            }
        }

        existing.setTexts(Stream.concat(existing.getTexts().entrySet().stream(),
                createdOrUpdated.entrySet().stream())
                .collect(collectingAndThen(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e2,
                        LinkedHashMap::new
                ), Collections::unmodifiableMap)));
    }
}
