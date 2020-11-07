package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class CatalystResolver implements GraphQLResolver<CatalystDto> {
    private final MultilingualService multilingualService;
    private final QuestionService questionService;
    private final ResolverHelper helper;

    public String getTitle(final CatalystDto catalyst, final DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(catalyst.getTitleId(), helper.getLanguageCode(environment));
    }

    //TODO: remove catalyst does not have description, this is kept for keeping compatibility with frontend
    public String getDescription(final CatalystDto catalyst) {
        return null;
    }

    public List<Question> getQuestions(final CatalystDto catalyst) {
        return questionService.getAllCatalystQuestions(catalyst.getId(), catalyst.getCustomer(), catalyst.getSegment());
    }

    public List<DriverDto> getDrivers(final CatalystDto catalyst) {
        return questionService.getAllCatalystDrivers(catalyst);
    }
}
