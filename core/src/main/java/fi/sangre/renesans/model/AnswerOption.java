package fi.sangre.renesans.model;

import lombok.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AnswerOption {

    private Integer index;
    private Integer value;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MultilingualKey title;

    @Column(name = "title_id", updatable = false, insertable = false)
    private Long titleId;
}
