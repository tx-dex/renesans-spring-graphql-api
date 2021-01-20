package fi.sangre.renesans.persistence.discussion.model;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)

@Entity
@Table(name = "discussion_actor")
public class ActorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "survey_id", nullable = false, updatable = false)
    private UUID surveyId;

    @Column(name = "respondent_id", nullable = false, updatable = false)
    private UUID respondentId;


}
