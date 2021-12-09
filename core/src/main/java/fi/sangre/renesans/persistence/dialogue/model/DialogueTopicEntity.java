package fi.sangre.renesans.persistence.dialogue.model;

import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import fi.sangre.renesans.persistence.model.Survey;
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
@Table(name = "dialogue_topic")
@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class DialogueTopicEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", referencedColumnName = "id", nullable = false, updatable = false)
    private Survey survey;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DialogueTopicQuestionEntity> questions;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DialogueTipEntity> tips;

    @CreatedDate
    @Column(name = "ctm", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @LastModifiedDate
    @Column(name = "mtm", nullable = false)
    private LocalDateTime updatedOn;
}
