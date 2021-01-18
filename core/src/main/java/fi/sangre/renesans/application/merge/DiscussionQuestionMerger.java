package fi.sangre.renesans.application.merge;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionMerger {
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

        for (final DiscussionQuestion input : inputs) {
            final QuestionId id;
            if (input.getId() == null) {
                id = new QuestionId(UUID.randomUUID());
            } else {
                id = new QuestionId(input.getId().getValue());
            }

            combined.add(DiscussionQuestion.builder()
                    .id(id)
                    .active(input.isActive())
                    .title(input.getTitle())
                    .build());
        }

        return Collections.unmodifiableList(combined);
    }
}
