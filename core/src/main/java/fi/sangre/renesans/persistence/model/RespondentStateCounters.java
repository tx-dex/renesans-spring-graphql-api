package fi.sangre.renesans.persistence.model;

import fi.sangre.renesans.application.model.SurveyId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "surveyId")
public class RespondentStateCounters {
    private final SurveyId surveyId;
    private final long opened;
    private final long answering;
    private final long answered;
    private final long all;

    public RespondentStateCounters(final UUID id,
                                   final long opened,
                                   final long answering,
                                   final long answered,
                                   final long all) {
        this.surveyId = new SurveyId(id);
        this.opened = opened;
        this.answering = answering;
        this.answered = answered;
        this.all = all;
    }
}
