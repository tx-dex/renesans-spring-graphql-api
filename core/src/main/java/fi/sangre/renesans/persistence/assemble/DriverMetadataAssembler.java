package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
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
public class DriverMetadataAssembler {
    @NonNull
    public List<DriverMetadata> from(@NonNull final List<Driver> drivers) {
        return drivers.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public DriverMetadata from(@NonNull final Driver driver) {
        return DriverMetadata.builder()
                .id(driver.getId())
                .titles(driver.getTitles().getPhrases())
                .descriptions(driver.getDescriptions().getPhrases())
                .prescriptions(driver.getPrescriptions().getPhrases())
                .weight(driver.getWeight())
                .build();
    }
}
