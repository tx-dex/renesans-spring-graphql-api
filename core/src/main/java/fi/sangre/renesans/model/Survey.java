package fi.sangre.renesans.model;


import com.google.common.collect.Sets;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "survey")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Survey extends BaseModel {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    private Long seq;

    @OneToOne(fetch = FetchType.LAZY)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;

    @OneToOne(fetch = FetchType.LAZY)
    private MultilingualKey description;

    @Column(name = "description_id", updatable = false, insertable = false)
    private Long descriptionId;

    @Type(type = "jsonb")
    @Column(name = "metadata")
    private SurveyMetadata metadata;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "survey")
    @OrderBy("seq")
    @Builder.Default
    private List<QuestionGroup> questionGroups = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "survey")
    @Builder.Default
    private List<RespondentGroup> respondentGroups = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "survey")
    @Builder.Default
    private Set<Customer> organisations = Sets.newHashSet();


    //TODO: check if still used and remove
    // used to return respondent for questionnaire endpoint
    @Transient
    private Respondent respondent;

    //TODO: check if still used and remove
    // used to return respondentGroupId for questionnaire endpoint
    @Transient
    private String respondentGroupID;

    //TODO: use in dto and remove from here
    @Transient
    private Segment segment;
}
