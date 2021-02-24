package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.CatalystId;
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
public class CatalystMerger {
    private final DriverMerger driverMerger;
    private final QuestionsMerger questionsMerger;
    private final MultilingualUtils multilingualUtils;


    @NonNull
    public List<Catalyst> combine(@NonNull final List<Catalyst> existing, @Nullable final List<Catalyst> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing);
        } else {
            final ImmutableList.Builder<Catalyst> builder = ImmutableList.builder();

            final Map<CatalystId, Catalyst> existingCatalysts = existing.stream()
                    .collect(collectingAndThen(toMap(Catalyst::getId, v -> v), Collections::unmodifiableMap));
            for (final Catalyst input : inputs) {
                builder.add(combine(existingCatalysts, input));
            }

            final List<Catalyst> combined = builder.build();

            if (combined.size() != existing.size()) {
                throw new SurveyException("Cannot remove catalysts. Provide all catalysts for updating");
            }

            return combined;
        }
    }

    @NonNull
    private Catalyst combine(@NonNull final Map<CatalystId, Catalyst> existing, @NonNull final Catalyst input) {
        return combine(Objects.requireNonNull(existing.get(input.getId()), "Not existing catalyst in the input"),
                input);
    }

    @NonNull
    private Catalyst combine(@NonNull final Catalyst existing, @NonNull final Catalyst input) {
        return Catalyst.builder()
                .id(existing.getId())
                .titles(multilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .descriptions(multilingualUtils.combine(existing.getDescriptions(), input.getDescriptions()))
                .drivers(driverMerger.combine(existing.getDrivers(), input.getDrivers()))
                .questions(questionsMerger.combineLikert(existing.getQuestions(), input.getQuestions()))
                .openQuestions(questionsMerger.combineOpen(existing.getOpenQuestions(), input.getOpenQuestions()))
                .weight(input.getWeight())
                .build();
    }
}
