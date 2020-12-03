package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class MultilingualTextAssembler {
    @NonNull
    public MultilingualText fromOptional(@Nullable final String phrase, @NonNull final String languageTag) {
        return new MultilingualText(Optional.ofNullable(StringUtils.trimToNull(phrase))
                .map(v -> ImmutableMap.of(languageTag, phrase))
                .orElse(ImmutableMap.of()));
    }

    @NonNull
    public MultilingualText fromRequired(@Nullable final String phrase, @NonNull final String languageTag) {
        return new MultilingualText(Optional.ofNullable(StringUtils.trim(phrase))
                .map(v -> ImmutableMap.of(languageTag, phrase))
                .orElseThrow(() -> new SurveyException("Text is required")));
    }

    @NonNull
    public MultilingualText from(@Nullable final Map<String, String> input) {
        if (input == null) {
            return new MultilingualText(new HashMap<>());
        } else {
            return new MultilingualText(new HashMap<>(input));
        }
    }
}
