package fi.sangre.renesans.graphql.input.answer;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class LikertQuestionRateInput {
    private UUID questionId;
    private Integer rate;
}
