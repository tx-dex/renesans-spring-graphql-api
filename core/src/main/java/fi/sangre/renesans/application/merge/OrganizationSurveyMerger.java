package fi.sangre.renesans.application.merge;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyMerger {
    private final StaticTextMerger staticTextMerger;
    private final ParameterMerger parameterMerger;
    private final CatalystMerger catalystMerger;

    @NonNull
    public OrganizationSurvey combine(@NonNull final OrganizationSurvey existing, @NonNull final OrganizationSurvey input) {
        //TODO: check version here
        existing.setVersion(input.getVersion());
        existing.setLogo(input.getLogo());
        existing.setCatalysts(catalystMerger.combine(existing.getCatalysts(), input.getCatalysts()));
        existing.setParameters(parameterMerger.combine(existing.getParameters(), input.getParameters()));
        existing.setStaticTexts(staticTextMerger.combine(existing.getStaticTexts(), input.getStaticTexts()));

        return existing;
    }
}
