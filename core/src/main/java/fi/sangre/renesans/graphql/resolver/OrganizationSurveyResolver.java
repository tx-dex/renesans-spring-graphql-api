package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.assemble.SurveyMediaAssembler;
import fi.sangre.renesans.graphql.assemble.SurveyParameterOutputAssembler;
import fi.sangre.renesans.graphql.assemble.media.MediaDetailsAssembler;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.graphql.output.StaticTextGroupOutput;
import fi.sangre.renesans.graphql.output.StaticTextOutput;
import fi.sangre.renesans.graphql.output.media.MediaDetailsOutput;
import fi.sangre.renesans.graphql.output.media.SurveyMediaOutput;
import fi.sangre.renesans.graphql.output.parameter.SurveyParameterOutput;
import fi.sangre.renesans.service.TranslationService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor

@Component
public class OrganizationSurveyResolver implements GraphQLResolver<OrganizationSurvey> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;
    private final SurveyParameterOutputAssembler surveyParameterOutputAssembler;
    private final TranslationService translationService;
    private final MultilingualUtils multilingualUtils;
    private final SurveyMediaAssembler surveyMediaAssembler;
    private final MediaDetailsAssembler mediaDetailsAssembler;

    @NonNull
    public String getTitle(@NonNull final OrganizationSurvey survey, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(survey.getTitles(),
                resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getDescription(@NonNull final OrganizationSurvey survey, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getOptionalText(survey.getDescriptions(),
                resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public MediaDetailsOutput getLogo(@NonNull final OrganizationSurvey survey) {
        return mediaDetailsAssembler.from(survey.getLogo());
    }

    @NonNull
    public Collection<SurveyMediaOutput> getMedia(@NonNull final OrganizationSurvey survey) {
        return surveyMediaAssembler.from(survey.getMedia());
    }

    @NonNull
    public List<CatalystProxy> getCatalysts(@NonNull final OrganizationSurvey survey) {
        return toProxies(survey.getCatalysts());
    }

    @NonNull
    public List<SurveyParameterOutput> getParameters(@NonNull final OrganizationSurvey survey) {
        return surveyParameterOutputAssembler.from(survey.getParameters());
    }

    @NonNull
    public List<StaticTextGroupOutput> getStaticTexts(@NonNull final OrganizationSurvey survey, @NonNull final DataFetchingEnvironment environment) {
        final String languageTag = resolverHelper.getLanguageCode(environment);

        final StaticTextGroup emptyGroup = StaticTextGroup.builder()
                .texts(ImmutableMap.of())
                .build();

        return translationService.getTranslations(languageTag).entrySet().stream()
                .map(group -> StaticTextGroupOutput.builder()
                        .id(group.getKey())
                        .title(translationService.getTitle(group.getKey(), null))
                        .texts(group.getValue().entrySet().stream()
                                .map(text -> StaticTextOutput.builder()
                                        .id(text.getKey())
                                        .title(translationService.getTitle(text.getKey(), text.getValue().getTitle()))
                                        .description(text.getValue().getDescription())
                                        .text(survey.getStaticTexts().getOrDefault(group.getKey(), emptyGroup)
                                                .getTexts().getOrDefault(text.getKey(), multilingualUtils.empty())
                                                .getPhrases().getOrDefault(languageTag, text.getValue().getText()))
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public RespondentCounters respondentCounts(@NonNull final OrganizationSurvey survey) {
        if (survey.getRespondentCounters() == null) {
            return RespondentCounters.empty();
        } else {
            return survey.getRespondentCounters();
        }
    }
}
