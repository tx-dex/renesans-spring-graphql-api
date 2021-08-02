package fi.sangre.renesans.application.merge;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.UUIDUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionMerger {
    private final MultilingualUtils multilingualUtils;
    private final UUIDUtils uuidUtils;

    @NonNull
    public List<DiscussionQuestion> combine(@NonNull final List<DiscussionQuestion> existing, @Nullable final List<DiscussionQuestion> input) {
        if (input == null) {
            return ImmutableList.copyOf(existing);
        } else {
            return combineList(existing.stream()
                    .collect(collectingAndThen(toMap(
                            DiscussionQuestion::getId,
                            v -> v
                    ), Collections::unmodifiableMap)), input);
        }
    }

    @NonNull
    private List<DiscussionQuestion> combineList(@NonNull final Map<QuestionId, DiscussionQuestion> existing, @NonNull final List<DiscussionQuestion> inputs) {
        final List<DiscussionQuestion> combined = Lists.newArrayList();
        final Set<UUID> existingIds = new HashSet<>(uuidUtils.toUUIDs(existing.keySet()));

        for (final DiscussionQuestion input : inputs) {
            final DiscussionQuestion newOrExisting;
            if (input.getId() == null) {
                final UUID id = uuidUtils.generate(existingIds);
                newOrExisting = DiscussionQuestion.builder()
                        .id(new QuestionId(id))
                        .active(input.isActive())
                        .createdDate(new Date())
                        .title(input.getTitle())
                        .build();

                existingIds.add(id);
            } else {
                newOrExisting = Objects.requireNonNull(existing.get(input.getId()), "Not existing question in the input");
            }

            combined.add(combine(newOrExisting, input));
        }

        return Collections.unmodifiableList(combined);
    }

    @NonNull
    private DiscussionQuestion combine(@NonNull final DiscussionQuestion existing, @NonNull final DiscussionQuestion input) {
        return DiscussionQuestion.builder()
                .id(existing.getId())
                .active(input.isActive())
                .title(multilingualUtils.combine(existing.getTitle(), input.getTitle()))
                .createdDate(existing.getCreatedDate())
                .build();
    }
}
