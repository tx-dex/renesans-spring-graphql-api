package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.graphql.output.media.MediaDetailsOutput;
import fi.sangre.renesans.graphql.output.media.SurveyMediaOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireOutput {
    private UUID id;
    private String selectedLanguage;
    private MediaDetailsOutput logo;
    private List<SurveyMediaOutput> media;
    private List<QuestionnaireCatalystOutput> catalysts;
    private boolean hideCatalystThemePages;
    private List<QuestionnaireParameterOutput> parameters;
    private Map<String, StaticTextGroup> staticTexts;
    private boolean consented;
    private boolean finished;
    private boolean isAfterGameGuest;
    private boolean answerable;
    private boolean canAnswerParameters;
    private boolean canGoToQuestions;
    private boolean canAnswer;
    private boolean canViewAfterGame;
    private boolean canComment;
    private boolean isDialogueActive;


}
