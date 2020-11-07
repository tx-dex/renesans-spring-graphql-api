package fi.sangre.renesans.model;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@DynamicUpdate
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Segment extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "segment")
    @Builder.Default
    private Set<Customer> customers = Sets.newHashSet();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "segment")
    @OrderBy("seq")
    @Builder.Default
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Question> questions = Lists.newArrayList();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "segment")
    @Builder.Default
    private List<SegmentQuestionGroupPhrase> segmentQuestionGroupPhrases = Lists.newArrayList();
}
