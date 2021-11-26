package fi.sangre.renesans.graphql.output.dialogue;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueTotalStatisticsOutput {
    private UUID id;
    private Integer activeTopicsCount;
    private Integer answersCount;
    private Integer likesCount;
}
