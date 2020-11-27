package fi.sangre.renesans.graphql.input;

import fi.sangre.renesans.graphql.input.question.LikertQuestionInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class CatalystInput {
    private UUID id;
    private String title;
    private String description;
    private List<DriverInput> drivers;
    private List<LikertQuestionInput> questions;
}
