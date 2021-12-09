package fi.sangre.renesans.persistence.dialogue.model;

import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import fi.sangre.renesans.persistence.discussion.model.LikeEntity;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "dialogue_comment")
@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class DialogueCommentEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    private DialogueCommentEntity parent;

    @OneToMany(mappedBy="parent")
    private List<DialogueCommentEntity> replies;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DialogueCommentLikeEntity> likes;

    @ManyToOne(fetch = FetchType.LAZY)
    private DialogueTopicQuestionEntity question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", referencedColumnName = "id")
    private SurveyRespondent respondent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", referencedColumnName = "id", nullable = false, updatable = false)
    private Survey survey;

    @Column(name = "text")
    private String text;

    @CreatedDate
    @Column(name = "ctm", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column(name = "mtm", nullable = false)
    private LocalDateTime updatedOn;
}
