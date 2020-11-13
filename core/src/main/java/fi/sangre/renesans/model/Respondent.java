package fi.sangre.renesans.model;

import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(
        name = "respondent",
        indexes = {
                @Index(name = "age_idx", columnList = "age"),
                @Index(name = "country_idx", columnList = "country"),
                @Index(name = "gender_idx", columnList = "gender"),
                @Index(name = "industry_idx", columnList = "industry_id"),
                @Index(name = "position_idx", columnList = "position_id"),
                @Index(name = "respondentgroup_idx", columnList = "respondentgroup_id"),
                @Index(name = "segment_idx", columnList = "segment_id")
        })

// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE data.respondent SET archived = true WHERE id = ?")
@Loader(namedQuery = "findRespondentById")
@NamedQuery(name = "findRespondentById", query = "SELECT r FROM Respondent r WHERE r.id = ?1 AND r.archived = false")
@Where(clause = "archived = false")
@DynamicUpdate
public class Respondent extends BaseModel {
    public enum State {
        INVITED,
        STARTED,
        FINISHED
    }

    public Respondent(Respondent from) {
        id = from.getId();
        name = from.getName();
        email = from.getEmail();
        age = from.getAge();
        phone = from.getPhone();
        gender = from.getGender();
        country = from.getCountry();
        experience = from.getExperience();
        consent = from.getConsent();
        invitationHash = from.getInvitationHash();
        state = from.getState();
        locale = from.getLocale();
        answerTime = from.getAnswerTime();

        industry = from.getIndustry();
        position = from.getPosition();
        segment = from.getSegment();
        respondentGroup = from.getRespondentGroup();
        respondentGroupId = from.getRespondentGroupId();

        originalId = from.getOriginalId() != null ? from.getOriginalId() : from.getId(); // keep always original id from the parent even if it is a copy of copy of respondent

        answers = from.getAnswers().stream().map(Answer::new).collect(Collectors.toList());
    }

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String name;
    private String email;
    private Long age;
    private String phone;
    private String gender;
    private String country;
    private Long experience;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "respondent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @ManyToOne
    private RespondentOption industry;

    @ManyToOne
    private RespondentOption position;

    @ManyToOne
    private RespondentOption segment;

    @ManyToOne
    @JoinColumn(name = "respondentgroup_id")
    private RespondentGroup respondentGroup;

    @Column(name = "respondentgroup_id", updatable = false, insertable = false)
    private String respondentGroupId;

    private Boolean consent;

    @Column(name = "invitation_hash")
    private String invitationHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private State state = State.FINISHED;

    @Column
    private String locale;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="answer_time")
    private Date answerTime;

    // the id of the object that the copy is made from
    @Column(name = "original_id")
    private String originalId;
}
