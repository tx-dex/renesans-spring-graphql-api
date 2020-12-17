package fi.sangre.renesans.persistence.model;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;
}
