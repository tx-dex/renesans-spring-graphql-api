package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.DriverWeight;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionsMerger {
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<LikertQuestion> combine(@NonNull final List<LikertQuestion> existing, @Nullable final List<LikertQuestion> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing); // just return existing is user was not changing the questions
        } else {
            final ImmutableList.Builder<LikertQuestion> combined = ImmutableList.builder();

            final Map<QuestionId, LikertQuestion> existingQuestions = existing.stream()
                    .collect(collectingAndThen(toMap(LikertQuestion::getId, e -> e), Collections::unmodifiableMap));
            for (final LikertQuestion input : inputs) {
                combined.add(combine(existingQuestions, input));
            }

            return combined.build();
        }
    }

    @NonNull
    private LikertQuestion combine(@NonNull final Map<QuestionId, LikertQuestion> existing, @NonNull final LikertQuestion input) {
        final LikertQuestion newOrExisting;
        if (input.getId() == null) {
            newOrExisting = LikertQuestion.builder()
                    .id(new QuestionId(UUID.randomUUID()))
                    .catalystId(input.getCatalystId())
                    .weights(ImmutableList.of())
                    .titles(multilingualUtils.empty())
                    .build();
        } else {
            newOrExisting = Objects.requireNonNull(existing.get(input.getId()), "Not existing question in the input");
        }

        return combine(newOrExisting, input);

    }

    @NonNull
    private LikertQuestion combine(@NonNull final LikertQuestion existing, @NonNull final LikertQuestion input) {
        return LikertQuestion.builder()
                .id(existing.getId())
                .titles(multilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .weights(combineWeights(existing.getWeights(), input.getWeights()))
                .build();
    }

    @NonNull
    private List<DriverWeight> combineWeights(@Nullable final List<DriverWeight> existing, @Nullable final List<DriverWeight> inputs) {
        return ImmutableList.copyOf(Stream.concat(
                Optional.ofNullable(existing).orElse(ImmutableList.of()).stream(),
                Optional.ofNullable(inputs).orElse(ImmutableList.of()).stream())
                .collect(toMap(
                        e -> e.getDriver().getId(),
                        e -> e,
                        (e1, e2) -> DriverWeight.builder()
                                .driver(e2.getDriver())
                                .question(e2.getQuestion())
                                .weight(e2.getWeight())
                                .build()
                )).values());
    }
}
