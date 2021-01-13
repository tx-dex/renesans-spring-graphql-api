package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.graphql.input.media.MediaParametersInput;
import fi.sangre.renesans.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.net.URL;

@RequiredArgsConstructor
@Slf4j

@Component
public class CommonQueries implements GraphQLQueryResolver {
    private final MediaService mediaService;

    @Nullable
    @PreAuthorize("isAuthenticated()")
    public URL getMediaUrl(@NonNull final String key, @Nullable final MediaParametersInput params) {
        return mediaService.getMediaUrl(key, params);
    }

    @Deprecated
    @Nullable
    @PreAuthorize("isAuthenticated()")
    public URL getImageUrl(@NonNull final String key, @Nullable final MediaParametersInput params) {
        return mediaService.getMediaUrl(key, params);
    }
}
