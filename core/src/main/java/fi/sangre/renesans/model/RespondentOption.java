package fi.sangre.renesans.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "respondent_option")
public class RespondentOption extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long index;

    private OptionType optionType;

    @OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;

    public enum OptionType {
        INDUSTRY,
        POSITION,
        CLIENT_SEGMENT
    }
}
