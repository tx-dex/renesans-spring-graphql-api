package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.dto.CatalystDto;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class OrganizationSurveyResolver implements GraphQLResolver<OrganizationSurvey> {

    @NonNull
    public String getTitle(@NonNull final OrganizationSurvey survey) {
        //TODO: implement properly for getting based on context language
        return survey.getMetadata().getTitles().get("en");
    }

    @NonNull
    public String getDescription(@NonNull final OrganizationSurvey survey) {
        //TODO: implement properly for getting based on context language
        return survey.getMetadata().getDescriptions().get("en");
    }

    @NonNull
    public List<CatalystDto> getCatalysts(@NonNull final OrganizationSurvey survey) {
        //TODO: implement
        return ImmutableList.of();
    }
}
