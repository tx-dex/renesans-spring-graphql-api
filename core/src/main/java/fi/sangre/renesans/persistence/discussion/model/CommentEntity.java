package fi.sangre.renesans.persistence.discussion.model;

import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static fi.sangre.renesans.persistence.discussion.model.CommentEntity.COMMENT_GRAPH;

@NamedEntityGraph(
        name = COMMENT_GRAPH,
        attributeNodes = {
                @NamedAttributeNode(value = "actor"),
                @NamedAttributeNode(value = "likes", subgraph = "comment-like-subgraph"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "comment-like-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("actor"),
                        }
                ),
        }
)


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "discussion_comment")
@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class CommentEntity {
    public static final String COMMENT_GRAPH = "discussion-comment-graph";

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", referencedColumnName = "id")
    private ActorEntity actor;

    @Column(name = "survey_id", nullable = false, updatable = false)
    private UUID surveyId;

    @Column(name = "question_id", nullable = false, updatable = false)
    private UUID questionId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "actor_id")
    private Map<Long, LikeEntity> likes;

    @Column(name = "text", nullable = false)
    private String text;

    @CreatedDate
    @Column(name = "ctm", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column(name = "mtm", nullable = false)
    private LocalDateTime updatedOn;

}
