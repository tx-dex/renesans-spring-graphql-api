package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.UUIDUtils;
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
    private final UUIDUtils uuidUtils;


    @Nullable
    public List<OpenQuestion>  combineOpen(@NonNull final List<OpenQuestion> existing, @Nullable final List<OpenQuestion> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing);
        } else {
            final ImmutableList.Builder<OpenQuestion> combined = ImmutableList.builder();

            final Map<QuestionId, OpenQuestion> existingQuestions = existing.stream()
                    .collect(collectingAndThen(toMap(OpenQuestion::getId, e -> e), Collections::unmodifiableMap));
            final Set<UUID> existingIds = new HashSet<>(uuidUtils.toUUIDs(existingQuestions.keySet()));

            for (final OpenQuestion input : inputs) {
                final OpenQuestion newOrExisting;
                if (input.getId() == null) {
                    final UUID id = uuidUtils.generate(existingIds);
                    newOrExisting = OpenQuestion.builder()
                            .id(new QuestionId(id))
                            .catalystId(input.getCatalystId())
                            .titles(multilingualUtils.empty())
                            .build();

                    existingIds.add(id);
                } else {
                    newOrExisting = Objects.requireNonNull(existingQuestions.get(input.getId()), "Not existing question in the input");
                }

                combined.add(combine(newOrExisting, input));
            }

            return combined.build();
        }
    }


    @NonNull
    private OpenQuestion combine(@NonNull final OpenQuestion existing, @NonNull final OpenQuestion input) {
        return OpenQuestion.builder()
                .id(existing.getId())
                .titles(multilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .build();
    }

    @NonNull
    public List<LikertQuestion> combineLikert(@NonNull final List<LikertQuestion> existing, @Nullable final List<LikertQuestion> inputs) {
        if (inputs == null) {
            return ImmutableList.copyOf(existing); // just return existing is user was not changing the questions
        } else {
            final ImmutableList.Builder<LikertQuestion> combined = ImmutableList.builder();

            final Map<QuestionId, LikertQuestion> existingQuestions = existing.stream()
                    .collect(collectingAndThen(toMap(LikertQuestion::getId, e -> e), Collections::unmodifiableMap));
            final Set<UUID> existingIds = new HashSet<>(uuidUtils.toUUIDs(existingQuestions.keySet()));

            for (final LikertQuestion input : inputs) {
                final LikertQuestion newOrExisting;
                if (input.getId() == null) {
                    final UUID id = uuidUtils.generate(existingIds);
                    newOrExisting = LikertQuestion.builder()
                            .id(new QuestionId(id))
                            .catalystId(input.getCatalystId())
                            .weights(ImmutableMap.of())
                            .titles(multilingualUtils.empty())
                            .build();

                    existingIds.add(id);
                } else {
                    newOrExisting = Objects.requireNonNull(existingQuestions.get(input.getId()), "Not existing question in the input");
                }

                combined.add(combine(newOrExisting, input));
            }

            return combined.build();
        }
    }

    @NonNull
    private LikertQuestion combine(@NonNull final LikertQuestion existing, @NonNull final LikertQuestion input) {
        return LikertQuestion.builder()
                .id(existing.getId())
                .titles(multilingualUtils.combine(existing.getTitles(), input.getTitles()))
                .subTitles(multilingualUtils.combine(existing.getSubTitles(), input.getSubTitles()))
                .lowEndLabels(multilingualUtils.combine(existing.getLowEndLabels(), input.getLowEndLabels()))
                .highEndLabels(multilingualUtils.combine(input.getHighEndLabels(), input.getHighEndLabels()))
                .weights(combineWeights(existing.getWeights(), input.getWeights()))
                .build();
    }

    @NonNull
    private Map<DriverId, Double> combineWeights(@Nullable final Map<DriverId, Double> existing, @Nullable final Map<DriverId, Double> inputs) {
        return Collections.unmodifiableMap(Stream.concat(
                Optional.ofNullable(existing).orElse(ImmutableMap.of()).entrySet().stream(),
                Optional.ofNullable(inputs).orElse(ImmutableMap.of()).entrySet().stream())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e2,
                        LinkedHashMap::new
                )));
    }
}
