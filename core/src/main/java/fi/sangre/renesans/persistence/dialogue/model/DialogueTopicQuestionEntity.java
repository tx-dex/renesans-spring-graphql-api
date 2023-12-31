package fi.sangre.renesans.persistence.dialogue.model;

import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"id", "title"})

@Entity
@Table(name = "dialogue_topic_question")
@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class DialogueTopicQuestionEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_topic_id", referencedColumnName = "id")
    private DialogueTopicEntity topic;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "image")
    private String image;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdOn DESC")
    private final Set<DialogueCommentEntity> comments = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<DialogueQuestionLikeEntity> likes = new HashSet<>();

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "sort_index")
    private Integer sortIndex;

    @CreatedDate
    @Column(name = "ctm", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column(name = "mtm", nullable = false)
    private LocalDateTime updatedOn;
}
