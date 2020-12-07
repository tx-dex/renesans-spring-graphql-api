package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.persistence.model.metadata.PhrasesGroupMetadata;
import fi.sangre.renesans.persistence.model.metadata.PhrasesMetadata;
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
public class TranslationMetadataAssembler {
    @NonNull
    public Map<String, PhrasesGroupMetadata> from(@NonNull final List<StaticTextGroup> groups) {
        return groups.stream()
                .collect(collectingAndThen(toMap(
                        StaticTextGroup::getId,
                        e -> PhrasesGroupMetadata.builder()
                                .title(e.getTitle())
                                .description(e.getDescription())
                                .phrases(e.getTexts().stream().collect(
                                        collectingAndThen(toMap(
                                                StaticText::getId,
                                                text -> PhrasesMetadata.builder()
                                                        .phrases(text.getTexts().getPhrases())
                                                        .build(),
                                                (e1, e2) -> e1,
                                                LinkedHashMap::new), Collections::unmodifiableMap)))
                                .build(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new), Collections::unmodifiableMap));
    }
}
