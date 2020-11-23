package fi.sangre.renesans.graphql.input;

import fi.sangre.renesans.graphql.input.question.LikertQuestionInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class CatalystInput {
    private Long id;
    private String title;
    private String description;
    private List<DriverInput> drivers;
    private List<LikertQuestionInput> questions;
}
