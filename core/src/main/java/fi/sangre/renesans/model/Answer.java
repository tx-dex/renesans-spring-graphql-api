package fi.sangre.renesans.model;

import lombok.*;
import org.hibernate.annotations.Loader;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "answer")

// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE dataserver.answer SET archived = true WHERE id = ?")
@Loader(namedQuery = "findAnswerById")
@NamedQuery(name = "findAnswerById", query = "SELECT a FROM Answer a WHERE a.id = ?1 AND a.archived = false")
@Where(clause = "archived = false")
public class Answer extends BaseModel {

    public Answer(Answer from) {
        this.id = from.getId();
        this.answerValue = from.getAnswerValue();
        this.answerIndex = from.getAnswerIndex();
        this.question = from.getQuestion();
        this.questionId = from.getQuestionId();
        this.respondent = from.getRespondent();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_value")
    private Integer answerValue;

    @Column(name = "answer_index")
    private Integer answerIndex;

    @OneToOne(fetch = FetchType.LAZY)
    private Question question;

    @Column(name = "question_id", updatable = false, insertable = false)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Respondent respondent;
}
