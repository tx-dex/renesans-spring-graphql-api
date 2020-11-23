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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionsMerger {
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
        return combine(Objects.requireNonNull(existing.get(input.getId()), "Not existing driver in the input"),
                input);

    }

    @NonNull
    private LikertQuestion combine(@NonNull final LikertQuestion existing, @NonNull final LikertQuestion input) {
        return LikertQuestion.builder()
                .id(existing.getId())
                .titles(MultilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .weights(combineWeights(existing.getWeights(), input.getWeights()))
                .build();
    }

    @NonNull
    private List<DriverWeight> combineWeights(@NonNull final List<DriverWeight> existing, @NonNull final List<DriverWeight> inputs) {
        return ImmutableList.copyOf(Stream.concat(
                existing.stream(),
                inputs.stream())
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
