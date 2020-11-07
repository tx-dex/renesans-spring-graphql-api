package fi.sangre.renesans.graphql.input;

import lombok.Data;

@Data
public class AnswerInput {
    private Long id;
    private Integer answerIndex;
    private Long questionId;
}

