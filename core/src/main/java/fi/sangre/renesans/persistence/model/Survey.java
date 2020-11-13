package fi.sangre.renesans.persistence.model;


import com.google.api.client.util.Lists;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "survey")
@SQLDelete(sql = "UPDATE data.survey SET archived = true, version = version + 1 WHERE id = ? and version = ?")
@Where(clause = "archived = false")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Survey extends BaseModel {
    private static final Long INITIAL_VERSION = 1L;
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false,  columnDefinition = "uuid")
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = INITIAL_VERSION;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

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

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "surveys")
    @Builder.Default
    private List<Customer> organisations = Lists.newArrayList();


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
