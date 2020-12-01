package fi.sangre.renesans.persistence.model.answer;

import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder

@NamedEntityGraph(
        name = "parameter-answer-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "survey"),
                @NamedAttributeNode(value = "respondent"),
        }
)

@Entity
@Table(name = "parameter_answer")
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class ParameterAnswerEntity {
    @EmbeddedId
    protected ParameterAnswerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", updatable = false, insertable = false)
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", updatable = false, insertable = false)
    private SurveyRespondent respondent;

    @Column(name = "root_id", updatable = false)
    private UUID rootId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "type", updatable = false, nullable = false)
    @Enumerated(EnumType.STRING)
    private ParameterAnswerType type;

    @CreatedDate
    @Column(name = "answer_time", nullable = false, updatable = false)
    private LocalDateTime answerTime;
}
