package fi.sangre.renesans.graphql.assemble.discussion;

import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameDiscussionAssembler {
    @NonNull
    public Collection<AfterGameDiscussionOutput> fromList(@NonNull final List<DiscussionQuestion> questions) {
        return questions.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));


    }

    @NonNull
    public AfterGameDiscussionOutput from(@NonNull final DiscussionQuestion question) {
        return AfterGameDiscussionOutput.builder()
                .id(question.getId().getValue())
                .active(question.isActive())
                .numberOfAllComments(0L)
                .titles(question.getTitle().getPhrases())
                .build();
    }
}
