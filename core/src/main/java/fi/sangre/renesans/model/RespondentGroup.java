package fi.sangre.renesans.model;

import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "respondent_group")

// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE data.respondent_group SET archived = true, mtm = current_timestamp WHERE id = ?")
@Loader(namedQuery = "findRespondentGroupById")
@NamedQuery(name = "findRespondentGroupById", query = "SELECT g FROM RespondentGroup g WHERE g.id = ?1 AND g.archived = false")
@Where(clause = "archived = false")
@DynamicUpdate
public class RespondentGroup extends BaseModel {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Survey survey;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "respondentGroup", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Respondent> respondents = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "customer_Id")
    private Customer customer;

    @Column(name = "customer_id", updatable = false, insertable = false)
    private Long customerId;

    @ManyToMany(fetch = FetchType.LAZY)
    @OrderBy("seq")
    @JoinTable(
            name = "respondent_group_question_groups",
            joinColumns = @JoinColumn(
                    name = "respondent_group_id",
                    referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "question_group_id", referencedColumnName = "id"))
    private List<QuestionGroup> questionGroups;

    @Column(name = "default_locale", columnDefinition = "TEXT")
    private String defaultLocale;
}
