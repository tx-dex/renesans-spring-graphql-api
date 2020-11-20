package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.persistence.model.Survey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyAssembler {
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;

    @NonNull
    public OrganizationSurvey from(@NonNull final Survey survey) {
        return OrganizationSurvey.builder()
                .id(survey.getId())
                .version(survey.getVersion())
                .metadata(survey.getMetadata())
                .parameters(parameterAssembler.fromMetadata(survey.getMetadata().getParameters()))
                .staticTexts(staticTextAssembler.fromMetadata(survey.getMetadata().getStaticTexts()))
                .build();
    }
}
