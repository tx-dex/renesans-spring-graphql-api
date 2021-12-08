package fi.sangre.renesans.persistence.model;

import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder

@Entity
@Table(name = "survey_respondent")

@SQLDelete(sql = "UPDATE data.survey_respondent SET archived = true WHERE id = ?")
@Where(clause = "archived = false")

@DynamicInsert
@DynamicUpdate

@EntityListeners({ AuditingEntityListener.class, SecurityAuditorAware.class })
public class SurveyRespondent {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "survey_id", nullable = false, updatable = false)
    private UUID surveyId;

    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "invitation_hash", nullable = false)
    private String invitationHash;

    @Column(name = "invitation_error")
    private String invitationError;

    @Builder.Default
    @Column(name = "consent", nullable = false)
    private Boolean consent = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private SurveyRespondentState state = SurveyRespondentState.INVITING;

    @Column(name = "language_tag")
    private String selectedLanguageTag;

    @Column(name = "color")
    private String color;

    @Column(name = "invitation_language_tag", nullable = false)
    @Builder.Default
    private String invitationLanguageTag = "en";

    @Builder.Default
    @Column(name = "archived", nullable = false)
    private Boolean archived = false;

    @LastModifiedDate
    @Column(name = "mtm", nullable = false)
    private LocalDateTime modifiedOn;

    @CreatedDate
    @Column(name = "ctm", nullable = false, updatable = false)
    private LocalDateTime createdOn;
}
