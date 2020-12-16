package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.SurveyId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyOutputAssembler {

    @NonNull
    //TODO: create OrganizationSurveyOutput type
    public List<OrganizationSurvey> from(@NonNull final List<OrganizationSurvey> surveys,
                                         @NonNull final Map<SurveyId, RespondentCounters> counters) {

        surveys.forEach(e -> e.setRespondentCounters(
                counters.getOrDefault(new SurveyId(e.getId()), RespondentCounters.empty())));

        return surveys;
    }
}
