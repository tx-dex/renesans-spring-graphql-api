package fi.sangre.renesans.model;

import com.google.common.collect.Lists;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "question")
@Where(clause = "archived = false")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicInsert
@DynamicUpdate
public class Question extends BaseModel {
    public enum QuestionType {
        DEFAULT,
        INVERTED,
        DUAL
    }

    public enum SourceType {
        GENERIC,
        SEGMENT,
        ORGANISATION, // CUSTOMER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(nullable = false, updatable = false, insertable = false)
    private Long seq;

    @Column(name = "questiontype")
    @Builder.Default
    private QuestionType questionType = QuestionType.DEFAULT;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questiongroup_id")
    private QuestionGroup questionGroup;

    @Column(name = "questiongroup_id", updatable = false, insertable = false)
    private Long catalystId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "question")
    @OrderBy("questionGroupId")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Builder.Default
    private List<Weight> weights = Lists.newArrayList();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "customer_id", updatable = false, insertable = false)
    private Long customerId;
}
