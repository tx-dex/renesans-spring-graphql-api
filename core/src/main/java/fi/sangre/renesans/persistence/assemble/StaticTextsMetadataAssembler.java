package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.StaticText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class StaticTextsMetadataAssembler {
    @NonNull
    public Map<String, Map<String, String>> from(@NonNull final List<StaticText> texts) {
        return texts.stream()
                .collect(collectingAndThen(toMap(
                        StaticText::getId,
                        e -> e.getTexts().getPhrases(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new)
                , Collections::unmodifiableMap));
    }
}
