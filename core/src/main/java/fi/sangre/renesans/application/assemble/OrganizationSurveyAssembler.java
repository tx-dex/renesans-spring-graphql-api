package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyAssembler {
    private final MultilingualTextAssembler multilingualTextAssembler;
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;
    private final CatalystAssembler catalystAssembler;

    @NonNull
    public OrganizationSurvey fromParametersInput(@NonNull final UUID id,
                                                  @NonNull final Long version,
                                                  @NonNull final List<SurveyParameterInput> input,
                                                  @NonNull final String languageTag) {
        return OrganizationSurvey.builder()
                .id(id)
                .version(version)
                .parameters(parameterAssembler.fromInputs(input, languageTag))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromStaticTextInput(@NonNull final UUID id,
                                                  @NonNull final Long version,
                                                  @NonNull final StaticTextInput input,
                                                  @NonNull final String languageTag) {
        if (StringUtils.isNotBlank(input.getText())) {
            return OrganizationSurvey.builder()
                    .id(id)
                    .version(version)
                    .staticTexts(ImmutableList.of(StaticTextGroup.builder()
                            .id(input.getTextGroupId())
                            .texts(ImmutableList.of(staticTextAssembler.from(input, languageTag)))
                            .build()))
                    .build();
        } else {
            return OrganizationSurvey.builder()
                    .id(id)
                    .version(version)
                    .staticTexts(ImmutableList.of())
                    .build();
        }
    }

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
                .staticTexts(staticTextAssembler.fromMetadata(metadata.getTranslations()))
                .build();
    }
}
