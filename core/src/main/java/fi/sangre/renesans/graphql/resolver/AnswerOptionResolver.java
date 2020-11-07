package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.Context;
import fi.sangre.renesans.model.AnswerOption;
import fi.sangre.renesans.service.MultilingualService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AnswerOptionResolver implements GraphQLResolver<AnswerOption> {

    private final MultilingualService multilingualService;

    @Autowired
    public AnswerOptionResolver(MultilingualService multilingualService) {
        this.multilingualService = multilingualService;
    }

    public String getTitle(AnswerOption answerOption, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(answerOption.getTitleId(), getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getTitlePhraseId(AnswerOption answerOption) {
        return answerOption.getTitleId();
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Integer getValue(AnswerOption answerOption) {
        return answerOption.getValue();
    }

    private String getLanguageCode(DataFetchingEnvironment environment) {
        Context context = environment.getContext();
        return Locale.forLanguageTag(context.getLanguageCode()).getLanguage();
    }
}
