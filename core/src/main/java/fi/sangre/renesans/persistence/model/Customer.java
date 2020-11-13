package fi.sangre.renesans.persistence.model;

import com.google.api.client.util.Lists;
import fi.sangre.renesans.model.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "customer")

// TODO: rename to Organization when get rid of most of the stuff
// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE data.customer SET archived = true WHERE id = ?")
@Where(clause = "archived = false")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Customer extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @Builder.Default
    @OneToMany(mappedBy = "customer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RespondentGroup> groups = new ArrayList<>();

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private Long createdBy;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "survey_id", updatable = false, nullable = false)
    private Survey survey;

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
