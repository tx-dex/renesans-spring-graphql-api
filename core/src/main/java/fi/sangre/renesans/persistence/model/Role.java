package fi.sangre.renesans.persistence.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

@Data
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String title;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
    private Collection<User> users;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "roles_privileges",
            joinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "privilege_id", referencedColumnName = "id"))
    private Collection<Privilege> privileges;
}
