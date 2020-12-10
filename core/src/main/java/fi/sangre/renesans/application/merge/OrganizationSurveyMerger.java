package fi.sangre.renesans.application.merge;

import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyMerger {
    private final SurveyDao surveyDao;
    private final StaticTextMerger staticTextMerger;
    private final ParameterMerger parameterMerger;
    private final CatalystMerger catalystMerger;

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey combine(@NonNull final OrganizationSurvey input) {
        final OrganizationSurvey existing = surveyDao.getSurveyOrThrow(new SurveyId(input.getId()));
        //TODO: check version here
        existing.setVersion(input.getVersion());
        existing.setCatalysts(catalystMerger.combine(existing.getCatalysts(), input.getCatalysts()));
        existing.setParameters(parameterMerger.combine(existing.getParameters(), input.getParameters()));
        existing.setStaticTexts(staticTextMerger.combine(existing.getStaticTexts(), input.getStaticTexts()));

        return existing;
    }
}
