package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.model.MultilingualKey;
import fi.sangre.renesans.model.MultilingualPhrase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class MultilingualPhraseResolver implements GraphQLResolver<MultilingualPhrase> {


    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Long getId(MultilingualPhrase multilingualPhrase) {
        return multilingualPhrase.getId();
    }

    public String getText(MultilingualPhrase MultilingualPhrase) {
        if (MultilingualPhrase != null) {
            return MultilingualPhrase.getMessage();
        }
        return null;
    }

    public String getName(MultilingualPhrase multilingualPhrase) {
        MultilingualKey multilingualKey = multilingualPhrase.getKey();
        if (multilingualKey != null) {
            return multilingualKey.getKey();
        }
        return null;
    }
}
