package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class DriverMerger {
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<Driver> combine(@NonNull final List<Driver> existing, @Nullable final List<Driver> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing); // just return existing is user was not changing the drivers
        } else {
            final ImmutableList.Builder<Driver> builder = ImmutableList.builder();

            final Map<Long, Driver> existingDrivers = existing.stream()
                    .collect(collectingAndThen(toMap(Driver::getId, e -> e), Collections::unmodifiableMap));
            for (final Driver input : inputs) {
                builder.add(combine(existingDrivers, input));
            }

            final List<Driver> combined = builder.build();

            if (combined.size() != existing.size()) {
                throw new SurveyException("Cannot remove drivers. Provide all catalysts' drivers for updating");
            }

            return combined;
        }
    }

    @NonNull
    private Driver combine(@NonNull final Map<Long, Driver> existing, @NonNull final Driver input) {
        return combine(Objects.requireNonNull(existing.get(input.getId()), "Not existing driver in the input"),
                input);

    }

    @NonNull
    private Driver combine(@NonNull final Driver existing, @NonNull final Driver input) {
        return Driver.builder()
                .id(existing.getId())
                .titles(multilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .descriptions(multilingualUtils.combine(existing.getDescriptions(), input.getDescriptions()))
                .prescriptions(multilingualUtils.combine(existing.getPrescriptions(), input.getPrescriptions()))
                .weight(input.getWeight())
                .build();
    }
}
