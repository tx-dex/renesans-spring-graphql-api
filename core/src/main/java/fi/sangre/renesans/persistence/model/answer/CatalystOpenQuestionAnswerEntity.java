package fi.sangre.renesans.persistence.model.answer;

import fi.sangre.renesans.application.model.QuestionType;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.*;
import lombok.experimental.Accessors;
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
        name = "catalyst-answer-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "survey"),
                @NamedAttributeNode(value = "respondent"),
        }
)


@Entity
@Table(name = "open_question_answer")
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class CatalystOpenQuestionAnswerEntity {
    @EmbeddedId
    protected QuestionAnswerId id;

    @Column(name = "catalyst_id", updatable = false, nullable = false)
    private UUID catalystId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", updatable = false, insertable = false)
    private Survey survey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", updatable = false, insertable = false)
    private SurveyRespondent respondent;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private AnswerStatus status;

    @Column(name = "is_public")
    @Accessors(fluent = true)
    private Boolean isPublic;

    @Column(name = "open_response")
    private String response;

    @CreatedDate
    @Column(name="answer_time", nullable=false, updatable=false)
    private LocalDateTime answerTime;

    @Transient
    public QuestionType getQuestionType() {
        return QuestionType.OPEN;
    }
}
