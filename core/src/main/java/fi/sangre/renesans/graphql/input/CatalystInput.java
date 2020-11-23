package fi.sangre.renesans.graphql.input;

import fi.sangre.renesans.graphql.input.question.LikertQuestionInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CatalystInput {
    private Long id;
    private String title;
    private String description;
    private List<DriverInput> drivers;
    private List<LikertQuestionInput> questions;
}
