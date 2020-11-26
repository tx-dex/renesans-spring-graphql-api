package fi.sangre.renesans.graphql.resolver;

import fi.sangre.renesans.graphql.Context;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
public class ResolverHelper {
    private static final String PRINCIPAL_NOT_SET_MESSAGE = "Principal is not set in context";

    @NonNull
    public UserDetails getRequiredPrincipal(@NonNull final DataFetchingEnvironment environment) {
        final Context context = environment.getContext();

        return Objects.requireNonNull(context.getPrincipal(), PRINCIPAL_NOT_SET_MESSAGE);
    }

    public String getLanguageCode(@NonNull final DataFetchingEnvironment environment) {
        final Context context = environment.getContext();
        return Locale.forLanguageTag(context.getLanguageCode()).getLanguage();
    }

    public String getLanguageTag(@Nullable final String languageTag, @NonNull final DataFetchingEnvironment environment) {
        return Optional.ofNullable(languageTag)
                .orElseGet(() -> getLanguageCode(environment));
    }

    // This updates context on mutations when we want to save something in different language
    // then this one from accept-language header
    public void setLanguageCode(@Nullable final String languageTag, @NonNull final DataFetchingEnvironment environment) {
        if (languageTag != null) {
            final Context context = environment.getContext();
            context.setLanguageCode(languageTag);
        }
    }
}
