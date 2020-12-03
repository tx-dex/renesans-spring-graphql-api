package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyAssembler {
    private final MultilingualTextAssembler multilingualTextAssembler;
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;
    private final CatalystAssembler catalystAssembler;

    @NonNull
    public OrganizationSurvey from(@NonNull final Survey survey) {
        final SurveyMetadata metadata = survey.getMetadata();

        return OrganizationSurvey.builder()
                .id(survey.getId())
                .version(survey.getVersion())
                .titles(multilingualTextAssembler.from(metadata.getTitles()))
                .descriptions(multilingualTextAssembler.from(metadata.getDescriptions()))
                .catalysts(catalystAssembler.fromMetadata(metadata.getCatalysts()))
                .parameters(parameterAssembler.fromMetadata(metadata.getParameters()))
                .staticTexts(staticTextAssembler.fromMetadata(metadata.getStaticTexts()))
                .build();
    }
}
