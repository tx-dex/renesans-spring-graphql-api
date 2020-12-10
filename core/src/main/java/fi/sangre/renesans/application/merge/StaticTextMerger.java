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

import java.util.Map;

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
        final ImmutableMap.Builder<String, MultilingualText> builder = ImmutableMap.builder();

        for (final Map.Entry<String, MultilingualText> text : input.getTexts().entrySet()) {
            final MultilingualText found = existing.getTexts().get(text.getKey());

            if (found == null) {
                builder.put(text.getKey(), text.getValue());
            } else {
                builder.put(text.getKey(), multilingualUtils.combine(found, text.getValue()));
            }
        }

        existing.setTexts(builder.build());
    }
}
