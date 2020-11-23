package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.exception.MissingIdException;
import fi.sangre.renesans.graphql.input.CatalystInput;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystAssembler {
    private final QuestionAssembler questionAssembler;
    private final DriverAssembler driverAssembler;
    private final MultilingualTextAssembler multilingualTextAssembler;

    @NonNull
    public List<Catalyst> fromInput(@NonNull List<CatalystInput> inputs, @NonNull final String languageTag) {
        return inputs.stream()
                .map(e -> from(e, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public Catalyst from(@NonNull CatalystInput input, @NonNull final String languageTag) {
        return Catalyst.builder()
                .id(input.getId())
                .titles(multilingualTextAssembler.fromOptional(input.getTitle(), languageTag)) // This is optional as it may not be provided when updating questions
                .descriptions(multilingualTextAssembler.fromOptional(input.getDescription(), languageTag))
                .drivers(driverAssembler.fromInput(input.getDrivers(), languageTag))
                .questions(questionAssembler.fromInput(input.getQuestions(), languageTag))
                .build();
    }

    @NonNull
    public List<Catalyst> fromMetadata(@Nullable final List<CatalystMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Catalyst from(@NonNull final CatalystMetadata metadata) {
        return Catalyst.builder()
                .id(Objects.requireNonNull(metadata.getId(), MissingIdException.MESSAGE_SUPPLIER))
                .pdfName(metadata.getPdfName())
                .titles(new MultilingualText(metadata.getTitles()))
                .drivers(driverAssembler.fromMetadata(metadata.getDrivers()))
                .questions(questionAssembler.fromMetadata(metadata.getQuestions()))
                .weight(metadata.getWeight())
                .build();
    }
}
