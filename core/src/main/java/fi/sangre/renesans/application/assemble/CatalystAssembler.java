package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.MissingIdException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.CatalystInput;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
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
public class CatalystAssembler {
    private final QuestionAssembler questionAssembler;
    private final DriverAssembler driverAssembler;
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<Catalyst> fromInputs(@NonNull final List<CatalystInput> inputs, @NonNull final String languageTag) {
        if (new HashSet<>(inputs).size() != inputs.size()) {
            throw new SurveyException("Duplicated catalysts' keys in the input");
        }

        return inputs.stream()
                .map(e -> from(e, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Catalyst from(@NonNull CatalystInput input, @NonNull final String languageTag) {
        final CatalystId catalystId = new CatalystId(Objects.requireNonNull(input.getId(), "Catalyst id not provided in the input"));

        return Catalyst.builder()
                .id(catalystId)
                .titles(multilingualUtils.create(input.getTitle(), languageTag))
                .descriptions(multilingualUtils.create(input.getDescription(), languageTag))
                .drivers(driverAssembler.fromInput(input.getDrivers(), languageTag))
                .questions(Optional.ofNullable(input.getQuestions())
                        .map(questions -> questionAssembler.fromLikertInput(catalystId, questions, languageTag))
                        .orElse(null))
                .openQuestions(Optional.ofNullable(input.getOpenQuestions())
                        .map(questions -> questionAssembler.fromOpenInput(catalystId, questions, languageTag))
                        .orElse(null))
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
        final Catalyst catalyst = Catalyst.builder()
                .id(new CatalystId(Objects.requireNonNull(metadata.getId(), MissingIdException.MESSAGE_SUPPLIER)))
                .pdfName(metadata.getPdfName())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .descriptions(multilingualUtils.create(metadata.getDescriptions()))
                .drivers(driverAssembler.fromMetadata(metadata.getDrivers()))
                .weight(metadata.getWeight())
                .build();

        final List<LikertQuestion> likertQuestions = questionAssembler.fromLikertMetadata(catalyst, metadata.getQuestions());
        final List<OpenQuestion> openQuestions = questionAssembler.fromOpenMetadata(catalyst, metadata.getOpenQuestions());

        catalyst.setQuestions(likertQuestions);
        catalyst.setOpenQuestions(openQuestions);

        return catalyst;
    }
}
