package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.input.CatalystInput;
import fi.sangre.renesans.graphql.input.StaticTextInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyAssembler {
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;
    private final CatalystAssembler catalystAssembler;
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public OrganizationSurvey fromQuestionsInput(@NonNull final UUID id,
                                                 @NonNull final Long version,
                                                 @NonNull final List<CatalystInput> input,
                                                 @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);

        return OrganizationSurvey.builder()
                .id(id)
                .version(version)
                .catalysts(catalystAssembler.fromInputs(input, languageTag).stream()
                        .peek(e -> {
                            // we don't want to update drivers or titles
                            e.setTitles(multilingualUtils.empty());
                            e.setDescriptions(multilingualUtils.empty());
                            e.setDrivers(null);
                        })
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromCatalystAndDriversInput(@NonNull final UUID id,
                                                          @NonNull final Long version,
                                                          @NonNull final List<CatalystInput> input,
                                                          @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);

        return OrganizationSurvey.builder()
                .id(id)
                .version(version)
                .catalysts(catalystAssembler.fromInputs(input, languageTag).stream()
                        .peek(e -> {
                            // we don't want to update questions here
                            e.setOpenQuestion(multilingualUtils.empty());
                            e.setQuestions(null);
                        })
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    public OrganizationSurvey fromParametersInput(@NonNull final UUID id,
                                                  @NonNull final Long version,
                                                  @NonNull final List<SurveyParameterInput> input,
                                                  @NonNull final String languageTag) {
        multilingualUtils.checkLanguageTag(languageTag);

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
        multilingualUtils.checkLanguageTag(languageTag);

        if (StringUtils.isNotBlank(input.getText())) {
            return OrganizationSurvey.builder()
                    .id(id)
                    .version(version)
                    .staticTexts(ImmutableMap.of(input.getTextGroupId(), StaticTextGroup.builder()
                            .texts(ImmutableMap.of(input.getId(), staticTextAssembler.from(input, languageTag)))
                            .build()))
                    .build();
        } else {
            return OrganizationSurvey.builder()
                    .id(id)
                    .version(version)
                    .staticTexts(ImmutableMap.of())
                    .build();
        }
    }

    @NonNull
    public OrganizationSurvey from(@NonNull final Survey survey) {
        final SurveyMetadata metadata = survey.getMetadata();

        return OrganizationSurvey.builder()
                .id(survey.getId())
                .version(survey.getVersion())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .descriptions(multilingualUtils.create(metadata.getDescriptions()))
                .catalysts(catalystAssembler.fromMetadata(metadata.getCatalysts()))
                .parameters(parameterAssembler.fromMetadata(metadata.getParameters()))
                .staticTexts(staticTextAssembler.fromMetadata(metadata.getTranslations()))
                .build();
    }
}
