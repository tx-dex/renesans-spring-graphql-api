package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.dto.QuestionnaireDto;
import fi.sangre.renesans.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class QuestionnaireResolver implements GraphQLResolver<QuestionnaireDto> {
    private final QuestionnaireService questionnaireService;

    public List<CatalystDto> getQuestionGroups(final QuestionnaireDto questionnaire) {
        return questionnaireService.getCatalystsWithQuestions(questionnaire);
    }
}
