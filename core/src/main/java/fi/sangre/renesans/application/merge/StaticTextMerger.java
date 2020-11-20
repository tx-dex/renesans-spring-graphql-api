package fi.sangre.renesans.application.merge;


import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextMerger {
    @NonNull
    public List<StaticText> combine(@NonNull final List<StaticText> existing, @NonNull final StaticText input) {
        final ImmutableList.Builder<StaticText> combined = ImmutableList.builder();
        combined.addAll(existing);

        final Optional<StaticText> found = existing.stream()
                .filter(e -> e.equals(input))
                .findAny()
                .map(e -> {
                    e.setTexts(MultilingualUtils.combine(e.getTexts(), input.getTexts()));
                    return e;
                });

        if (!found.isPresent()) {
            combined.add(StaticText.builder()
                    .id(input.getId())
                    .texts(input.getTexts())
                    .build());
        }

        return combined.build();
    }


}
