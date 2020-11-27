package fi.sangre.renesans.graphql.input.answer;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class LikertQuestionAnswerInput {
    private UUID questionId;
    private Integer response;
}
