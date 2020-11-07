package fi.sangre.renesans.model;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "multilingual_key")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicInsert
public class MultilingualKey extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String key;
    //TODO: change to lazy
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "key")
    @MapKey(name = "locale")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Builder.Default
    Map<String, MultilingualPhrase> phrases = new HashMap<>();
}
