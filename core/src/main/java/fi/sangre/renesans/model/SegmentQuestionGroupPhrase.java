package fi.sangre.renesans.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "segment_question_group_phrase")
public class SegmentQuestionGroupPhrase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Segment segment;

    @OneToOne(fetch = FetchType.LAZY)
    private QuestionGroup questionGroup;

    @Column(name = "question_group_id", updatable = false, insertable = false)
    private Long questionGroupId;

    @OneToOne(fetch = FetchType.LAZY)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;
}
