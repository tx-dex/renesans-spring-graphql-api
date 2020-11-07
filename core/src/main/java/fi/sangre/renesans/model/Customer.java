package fi.sangre.renesans.model;

import com.google.api.client.util.Lists;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
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

// Enable soft deletes
// todo get schema from property file // https://hibernate.atlassian.net/browse/HHH-11028
@SQLDelete(sql = "UPDATE dataserver.customer SET archived = true WHERE id = ?")
@Loader(namedQuery = "findCustomerById")
@NamedQuery(name = "findCustomerById", query = "SELECT c FROM Customer c WHERE c.id = ?1 AND c.archived = false")
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

    @Column(name = "is_default", updatable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customer")
    @OrderBy("seq")
    @Builder.Default
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Question> questions = Lists.newArrayList();
}
