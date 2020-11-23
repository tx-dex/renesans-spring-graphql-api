package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystMetadataAssembler {
    private final DriverMetadataAssembler driverMetadataAssembler;
    private final QuestionMetadataAssembler questionMetadataAssembler;

    @NonNull
    public List<CatalystMetadata> from(@NonNull final List<Catalyst> catalysts) {
        return catalysts.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public CatalystMetadata from(@NonNull final Catalyst catalyst) {
        return CatalystMetadata.builder()
                .id(catalyst.getId())
                .titles(catalyst.getTitles().getPhrases())
                .drivers(driverMetadataAssembler.from(catalyst.getDrivers()))
                .questions(questionMetadataAssembler.from(catalyst.getQuestions()))
                .weight(catalyst.getWeight())
                .build();
    }
}
