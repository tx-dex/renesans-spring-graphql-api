package fi.sangre.renesans.graphql.assemble.discussion;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.output.discussion.AfterGameCommentOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.persistence.discussion.model.CommentEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameDiscussionAssembler {
    @NonNull
    public Collection<AfterGameDiscussionOutput> from(@NonNull final List<DiscussionQuestion> questions,
                                                      @NonNull final Map<QuestionId, List<CommentEntity>> discussions,
                                                      @Nullable final Long actorId) {
        return questions.stream()
                .map(question -> from(question,
                        discussions.getOrDefault(question.getId(), ImmutableList.of()),
                        actorId))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public AfterGameDiscussionOutput from(@NonNull final DiscussionQuestion question,
                                          @NonNull final List<CommentEntity> discussion,
                                          @Nullable final Long actorId) {
        return AfterGameDiscussionOutput.builder()
                .id(question.getId().getValue())
                .active(question.isActive())
                .comments(from(discussion, actorId))
                .numberOfAllComments((long) discussion.size())
                .titles(question.getTitle().getPhrases())
                .build();
    }

    @NonNull
    private List<AfterGameCommentOutput> from(@NonNull final List<CommentEntity> discussion,
                                              @Nullable final Long actorId) {
        return discussion.stream()
                .map(comment -> AfterGameCommentOutput.builder()
                .id(comment.getId())
                        .text(comment.getText())
                        .numberOfAllLikes((long) comment.getLikes().size())
                        .liked(actorId != null ? comment.getLikes().containsKey(actorId) : false)
                .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
