package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.Context;
import fi.sangre.renesans.model.RespondentOption;
import fi.sangre.renesans.service.MultilingualService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Deprecated
public class RespondentOptionResolver implements GraphQLResolver<RespondentOption> {
    private final MultilingualService multilingualService;

    @Autowired
    public RespondentOptionResolver(MultilingualService multilingualService) {
        this.multilingualService = multilingualService;
    }

    public String getTitle(RespondentOption respondentOption, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(respondentOption.getTitleId(), getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getTitlePhraseId(RespondentOption respondentOption) {
        return respondentOption.getTitleId();
    }

    private String getLanguageCode(DataFetchingEnvironment environment) {
        Context context = environment.getContext();
        return Locale.forLanguageTag(context.getLanguageCode()).getLanguage();
    }
}
