package fi.sangre.renesans.graphql.resolver;

import fi.sangre.renesans.graphql.Context;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ResolverHelper {
    public String getLanguageCode(@NonNull final DataFetchingEnvironment environment) {
        final Context context = environment.getContext();
        return Locale.forLanguageTag(context.getLanguageCode()).getLanguage();
    }

    public void setLanguageCode(@Nullable final String languageTag, @NonNull final DataFetchingEnvironment environment) {
        if (languageTag != null) {
            final Context context = environment.getContext();
            context.setLanguageCode(languageTag);
        }
    }
}
