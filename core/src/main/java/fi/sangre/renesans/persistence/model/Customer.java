package fi.sangre.renesans.persistence.model;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Sets;
import fi.sangre.renesans.model.BaseModel;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor

@NamedEntityGraph(
        name = "customer-owner-graph",
        attributeNodes = {
                @NamedAttributeNode(value = "owner", subgraph = "user-roles-subgraph"),
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "user-roles-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("roles"),
                        }
                ),
        }
)

@Entity
@Table(name = "customer")

// TODO: rename to Organization when get rid of most of the stuff
// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE data.customer SET archived = true WHERE id = ?")
@Where(clause = "archived = false")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class Customer extends BaseModel {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;
    private String name;
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RespondentGroup> groups = new ArrayList<>();

    @CreatedBy
    @Column(name = "created_by", updatable = false, nullable = false)
    private Long createdBy;

    @ManyToOne(optional = true)
    @JoinColumn(name = "created_by", updatable = false, insertable = false, nullable = true)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "customers_users",
            joinColumns = @JoinColumn(
                    name = "customer_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"))
    private Set<User> users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    private Segment segment;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "organizations_surveys",
            joinColumns = @JoinColumn(name = "organization_id"),
            inverseJoinColumns = @JoinColumn(name = "survey_id"))
    @Builder.Default
    private Set<Survey> surveys = Sets.newHashSet();

    @Column(name = "is_default", updatable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    @OrderBy("seq")
    @Builder.Default
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Question> questions = Lists.newArrayList();
}
