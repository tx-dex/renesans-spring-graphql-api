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
import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<DialogueCommentEntity> replies;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DialogueCommentLikeEntity> likes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialogue_topic_question_id", referencedColumnName = "id")
    private DialogueTopicQuestionEntity question;

    @ManyToOne(fetch = FetchType.EAGER)
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
