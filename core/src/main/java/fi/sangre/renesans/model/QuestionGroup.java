package fi.sangre.renesans.model;

import fi.sangre.renesans.persistence.model.Survey;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "question_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QuestionGroup extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long seq;

    @ManyToOne
    private QuestionGroup parent;

    @Column(name = "parent_id", updatable = false, insertable = false)
    private Long parentId;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private MultilingualKey description;

    @Column(name = "description_id", updatable = false, insertable = false)
    private Long descriptionId;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private MultilingualKey prescription;

    @Column(name = "prescription_id", updatable = false, insertable = false)
    private Long prescriptionId;

    @Column(name="pdfname")
    private String pdfName;

    private Double weight;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "questionGroup")
    @OrderBy("seq")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy="parent", cascade = CascadeType.ALL)
    @OrderBy("seq")
    @Builder.Default
    private List<QuestionGroup> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Survey survey;

    @ManyToMany(mappedBy = "questionGroups")
    private List<RespondentGroup> respondentGroups;
}
