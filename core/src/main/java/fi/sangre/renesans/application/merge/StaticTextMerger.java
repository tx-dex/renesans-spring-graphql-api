package fi.sangre.renesans.application.merge;


import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextMerger {
    @NonNull
    public List<StaticTextGroup> combine(@NonNull final List<StaticTextGroup> existing, @Nullable final List<StaticTextGroup> input) {
        if (input == null) {
            return ImmutableList.copyOf(existing);
        } else {
            final ImmutableList.Builder<StaticTextGroup> builder = ImmutableList.builder();
            builder.addAll(existing);

            for(final StaticTextGroup group : input) {
                final StaticTextGroup found = existing.stream()
                        .filter(e -> e.getId().equals(group.getId()))
                        .findAny()
                        .orElse(null);

                if (found == null) {
                    builder.add(group);
                } else {
                    combineGroup(found, group);
                }


            }

            return builder.build();
        }
    }

    private void combineGroup(@NonNull final StaticTextGroup existing, @NonNull final StaticTextGroup input) {
        final ImmutableList.Builder<StaticText> builder = ImmutableList.builder();
        builder.addAll(existing.getTexts());

        for (final StaticText text : input.getTexts()) {
            final StaticText found = existing.getTexts().stream()
                    .filter(e -> e.getId().equals(text.getId()))
                    .findAny()
                    .orElse(null);

            if (found == null) {
                builder.add(text);
            } else {
                found.setTexts(MultilingualUtils.combine(found.getTexts(), text.getTexts()));
            }
        }

        existing.setTexts(builder.build());
    }
}
