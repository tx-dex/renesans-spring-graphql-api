package fi.sangre.renesans.graphql;

import fi.sangre.renesans.service.MultilingualService;
import graphql.servlet.context.DefaultGraphQLContextBuilder;
import graphql.servlet.context.GraphQLContext;
import graphql.servlet.context.GraphQLServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import java.util.List;
import java.util.Optional;

@Component
public class ApiContextBuilder extends DefaultGraphQLContextBuilder {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String ACCEPT_LANGUAGE_HEADER = "accept-language";

    private final MultilingualService multilingualService;

    @Autowired
    public ApiContextBuilder(MultilingualService multilingualService) {
        this.multilingualService = multilingualService;
    }

    private String getLanguageTag(@NonNull final String acceptLanguage) {
        final String languageTag;
        // sometimes sent as full locale with other information - only take first 2 letters for now
        String shortLanguageCode = acceptLanguage.substring(0, 2);

        // check against all valid language codes
        List<String> validLanguageCodes = multilingualService.getValidLanguageCodes();
        if (validLanguageCodes.contains(shortLanguageCode)) {
            languageTag = shortLanguageCode;
        } else {
            languageTag = DEFAULT_LANGUAGE;
        }

        return languageTag;
    }

    @Override
    public GraphQLContext build(final HttpServletRequest request, final HttpServletResponse response) {
        final String languageCode = Optional.ofNullable(request.getHeader(ACCEPT_LANGUAGE_HEADER))
                .map(this::getLanguageTag)
                .orElse(DEFAULT_LANGUAGE);

        return new Context((GraphQLServletContext) super.build(request, response), languageCode);
    }

    @Override
    public GraphQLContext build(final Session session, final HandshakeRequest handshakeRequest) {
        return null; //TODO: implement for subscriptions
    }

    @Override
    public GraphQLContext build() {
        return null;
    }
}
