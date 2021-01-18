package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.persistence.model.metadata.discussion.DiscussionQuestionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionMetadataAssembler {
    @Nullable
    public List<DiscussionQuestionMetadata> from(@Nullable final List<DiscussionQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return null;
        } else {
            return questions.stream()
                    .map(question -> DiscussionQuestionMetadata.builder()
                            .id(Objects.requireNonNull(question.getId().getValue()))
                            .titles(question.getTitle().getPhrases())
                            .active(question.isActive())
                            .build())
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
    }
}
