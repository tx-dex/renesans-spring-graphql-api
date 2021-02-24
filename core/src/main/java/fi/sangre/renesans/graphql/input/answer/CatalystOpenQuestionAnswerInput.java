package fi.sangre.renesans.graphql.input.answer;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@ToString
public class CatalystOpenQuestionAnswerInput {
    private UUID questionId;
    private String response;
    @Accessors(prefix = "_")
    private Boolean _public;
}
