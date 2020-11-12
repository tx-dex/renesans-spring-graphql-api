package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.QuestionnaireDto;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;

@RequiredArgsConstructor

@Component
public class QuestionnaireResolver implements GraphQLResolver<QuestionnaireDto> {
    private final QuestionnaireService questionnaireService;

    @Deprecated
    public List<CatalystProxy> getQuestionGroups(final QuestionnaireDto questionnaire) {
        return toProxies(questionnaireService.getCatalystsWithQuestions(questionnaire));
    }

    @NonNull
    public List<CatalystProxy> getCatalysts(final QuestionnaireDto questionnaire) {
        return toProxies(questionnaireService.getCatalystsWithQuestions(questionnaire));
    }
}
