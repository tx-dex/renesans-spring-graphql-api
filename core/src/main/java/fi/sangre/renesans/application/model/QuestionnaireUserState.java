package fi.sangre.renesans.application.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class QuestionnaireUserState {
    private boolean consented;
    private boolean answeringParameters;
    private boolean answeringQuestions;
    private boolean viewingAfterGame;
}
