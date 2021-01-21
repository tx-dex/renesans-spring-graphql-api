package fi.sangre.renesans.persistence.model;


import com.google.api.client.util.Sets;
import fi.sangre.renesans.application.model.SurveyState;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
@Audited

@DynamicInsert
@DynamicUpdate

@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class Survey {
    private static final Long INITIAL_VERSION = 1L;

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @NotAudited
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @NotAudited
    @OneToOne(fetch = FetchType.LAZY)
    private MultilingualKey title;

    @NotAudited
    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;

    @NotAudited
    @OneToOne(fetch = FetchType.LAZY)
    private MultilingualKey description;

    @NotAudited
    @Column(name = "description_id", updatable = false, insertable = false)
    private Long descriptionId;

    @Type(type = "jsonb")
    @Column(name = "metadata")
    private SurveyMetadata metadata;

    @Column(name = "state", nullable = false)
    @Enumerated(EnumType.STRING)
    private SurveyState state;

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "survey")
    @OrderBy("seq")
    @Builder.Default
    private List<QuestionGroup> questionGroups = new ArrayList<>();

    @NotAudited
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "survey")
    @Builder.Default
    private List<RespondentGroup> respondentGroups = new ArrayList<>();

    @NotAudited
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "surveys")
    @Builder.Default
    private Set<Customer> organisations = Sets.newHashSet();

    @NotAudited
    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @LastModifiedBy
    @Column(name="muser", nullable=false)
    private Long modifiedBy;

    @NotAudited
    @CreatedBy
    @Column(name="cuser", nullable=false, updatable=false)
    private Long craetedBy;

    @LastModifiedDate
    @Column(name="mtm", nullable=false)
    private LocalDateTime modifiedOn;

    @NotAudited
    @CreatedDate
    @Column(name="ctm", nullable=false, updatable=false)
    private LocalDateTime createdOn;

    //TODO: check if still used and remove
    // used to return respondent for questionnaire endpoint
    @Transient
    private Respondent respondent;

    //TODO: use in dto and remove from here
    @Transient
    private Segment segment;
}
