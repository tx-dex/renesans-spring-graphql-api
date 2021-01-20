package fi.sangre.renesans.persistence.discussion.repository;

import fi.sangre.renesans.persistence.discussion.model.CommentEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fi.sangre.renesans.persistence.discussion.model.CommentEntity.COMMENT_GRAPH;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, UUID> {

    @NonNull
    @EntityGraph(COMMENT_GRAPH)
    List<CommentEntity> findAllBySurveyIdAndQuestionIdOrderByCreatedOnAsc(@NonNull UUID surveyId, @NonNull UUID questionId);
    @NonNull
    @EntityGraph(COMMENT_GRAPH)
    List<CommentEntity> findAllBySurveyIdAndQuestionIdInOrderByCreatedOnAsc(@NonNull UUID surveyId, @NonNull Set<UUID> questionIds);
}
