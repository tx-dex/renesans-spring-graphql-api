package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.graphql.assemble.SurveyParameterOutputAssembler;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.graphql.output.parameter.SurveyParameterOutput;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;

@RequiredArgsConstructor

@Component
public class OrganizationSurveyResolver implements GraphQLResolver<OrganizationSurvey> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;
    private final SurveyParameterOutputAssembler surveyParameterOutputAssembler;

    @NonNull
    public String getTitle(@NonNull final OrganizationSurvey survey, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(survey.getTitles().getPhrases(),
                resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getDescription(@NonNull final OrganizationSurvey survey, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getOptionalText(survey.getDescriptions().getPhrases(),
                resolverHelper.getLanguageCode(environment));
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
    public List<StaticText> getStaticTexts(@NonNull final OrganizationSurvey survey) {
        return survey.getStaticTexts();
    }

    @NonNull
    public RespondentCounters respondentCounts(@NonNull final OrganizationSurvey survey) {
        return RespondentCounters.builder().build();
    }
}
