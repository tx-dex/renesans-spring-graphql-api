package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.MissingIdException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.DriverInput;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class DriverAssembler {
    private final MultilingualUtils multilingualUtils;

    @Nullable
    public List<Driver> fromInput(@Nullable List<DriverInput> inputs, @NonNull final String languageTag) {
        if (inputs != null) {
            if (new HashSet<>(inputs).size() != inputs.size()) {
                throw new SurveyException("Duplicated drivers' keys in the input");
            }

            return inputs.stream()
                    .map(e -> from(e, languageTag))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return null;
        }
    }

    @NonNull
    public Driver from(@NonNull final DriverInput input, @NonNull final String languageTag) {
        final MultilingualText title = multilingualUtils.create(input.getTitle(), languageTag);

        if (title.isEmpty()) {
            throw new SurveyException("Title must not be empty");
        }

        return Driver.builder()
                .id(input.getId())
                .titles(title)
                .descriptions(multilingualUtils.create(input.getDescription(), languageTag))
                .prescriptions(multilingualUtils.create(input.getPrescription(), languageTag))
                .weight(input.getWeight())
                .build();
    }

    @NonNull
    public List<Driver> fromMetadata(@Nullable final List<DriverMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Driver from(@NonNull final DriverMetadata metadata) {
        return Driver.builder()
                .id(Objects.requireNonNull(metadata.getId(), MissingIdException.MESSAGE_SUPPLIER))
                .pdfName(metadata.getPdfName())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .descriptions(multilingualUtils.create(metadata.getDescriptions()))
                .prescriptions(multilingualUtils.create(metadata.getPrescriptions()))
                .weight(metadata.getWeight())
                .build();
    }
}
