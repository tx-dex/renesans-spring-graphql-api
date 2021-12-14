package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.media.Media;
import fi.sangre.renesans.application.model.media.MediaDetails;
import fi.sangre.renesans.application.model.parameter.Parameter;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class OrganizationSurvey {
    private UUID id;
    private Long version;
    private SurveyState state;
    private MediaDetails logo;
    private MultilingualText titles;
    private MultilingualText descriptions;
    private List<Media> media;
    private List<Catalyst> catalysts;
    private Boolean hideCatalystThemePages;
    private List<Parameter> parameters;
    private Map<String, StaticTextGroup> staticTexts;
    private List<DiscussionQuestion> discussionQuestions;
    @Builder.Default
    private boolean deleted = false;
    //TODO: create OrganizationSurveyOutput and move this there
    private RespondentCounters respondentCounters;
    private Boolean isDialogueActive;
}
