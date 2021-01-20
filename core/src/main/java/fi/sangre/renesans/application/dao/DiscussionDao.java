package fi.sangre.renesans.application.dao;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.persistence.discussion.model.ActorEntity;
import fi.sangre.renesans.persistence.discussion.model.CommentEntity;
import fi.sangre.renesans.persistence.discussion.repository.ActorRepository;
import fi.sangre.renesans.persistence.discussion.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionDao {
    private final ActorRepository actorRepository;
    private final CommentRepository commentRepository;

    @Nullable
    @Transactional(readOnly = true)
    public ActorEntity findActor(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return actorRepository.findBySurveyIdAndRespondentId(surveyId.getValue(), respondentId.getValue())
                .orElse(null);
    }

    @NonNull
    @Transactional
    public ActorEntity createOrGetActor(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        final ActorEntity actor = actorRepository.findBySurveyIdAndRespondentId(surveyId.getValue(), respondentId.getValue())
                .orElse(ActorEntity.builder()
                        .surveyId(surveyId.getValue())
                        .respondentId(respondentId.getValue())
                        .build());

        return actorRepository.save(actor);
    }

    @Transactional
    public void createOrUpdateComment(@NonNull final SurveyId surveyId, @NonNull final QuestionId questionId, @NonNull final ActorEntity actor, @Nullable final UUID commentId, @Nullable final String text) {
        final CommentEntity comment;
        if (commentId != null) {
            comment = commentRepository.findById(commentId)
                    .orElse(CommentEntity.builder()
                            .actor(actor)
                            .surveyId(surveyId.getValue())
                            .questionId(questionId.getValue())
                            .build());
        } else {
            comment = CommentEntity.builder()
                    .actor(actor)
                    .surveyId(surveyId.getValue())
                    .questionId(questionId.getValue())
                    .build();
        }

        comment.setText(Objects.requireNonNull(text));

        commentRepository.save(comment);
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<CommentEntity> findDiscussion(@NonNull final SurveyId surveyId, @NonNull final QuestionId questionId) {
        return commentRepository.findAllBySurveyIdAndQuestionIdOrderByCreatedOnAsc(surveyId.getValue(), questionId.getValue())
                .stream()
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<QuestionId, List<CommentEntity>> findDiscussions(@NonNull final SurveyId surveyId, @NonNull final Set<QuestionId> questionIds) {
        if (!questionIds.isEmpty()) {
            return commentRepository.findAllBySurveyIdAndQuestionIdInOrderByCreatedOnAsc(surveyId.getValue(), toUUIDs(questionIds))
                    .stream()
                    .collect(groupingBy(CommentEntity::getQuestionId))
                    .entrySet()
                    .stream()
                    .collect(collectingAndThen(toMap(
                            e -> new QuestionId(e.getKey()),
                            Map.Entry::getValue
                    ), Collections::unmodifiableMap));
        } else {
            return ImmutableMap.of();
        }
    }

    @NonNull
    private Set<UUID> toUUIDs(@NonNull final Set<QuestionId> ids) {
        return ids.stream()
                .map(QuestionId::getValue)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }
}
