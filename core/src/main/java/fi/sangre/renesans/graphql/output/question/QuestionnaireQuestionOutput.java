package fi.sangre.renesans.graphql.output.question;

import fi.sangre.renesans.application.model.questions.QuestionId;

public interface QuestionnaireQuestionOutput {
    QuestionId getId();
    boolean isAnswered();
    boolean isSkipped();
}
