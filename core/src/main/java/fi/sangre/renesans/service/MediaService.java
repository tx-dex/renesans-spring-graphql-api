package fi.sangre.renesans.service;

import fi.sangre.media.rest.api.dto.UploadUrlRequestDto;
import fi.sangre.media.rest.api.dto.UploadUrlResponseDto;
import fi.sangre.renesans.application.client.FeignMediaClient;
import fi.sangre.renesans.graphql.input.media.MediaParametersInput;
import fi.sangre.renesans.graphql.input.media.MediaUploadInput;
import fi.sangre.renesans.graphql.output.media.MediaUploadOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Service
public class MediaService {
    private static final String IMAGES_PATH = "images";
    private static final String MEDIA_SERVICE_IMG_PATH = "img/";
    private static final String MEDIA_SERVICE_RESIZE_FORMAT = "+resize-w%dh%d";
    private final FeignMediaClient feignMediaClient;

    @NonNull
    public MediaUploadOutput requestUploadUrl(@NonNull final MediaUploadInput input) {
        log.trace("Requesting upload url: input={}", input);

        try {
            final UploadUrlResponseDto response = feignMediaClient.requestUploadUrl(UploadUrlRequestDto.builder()
                    .filename(input.getFileName())
                    .mimeType(input.getMimeType())
                    .path(IMAGES_PATH)
                    .build());

            return MediaUploadOutput.builder()
                    .key(response.getKey())
                    .url(new URL(response.getUrl()))
                    .build();
        } catch (final Exception ex) {
            throw new RuntimeException("Cannot get upload url from media service", ex);
        }
    }

    @NonNull
    public Collection<String> getImageUrls(@NonNull final Collection<String> imageKeys, @Nullable final MediaParametersInput params) {
        return imageKeys.stream()
                .map(e -> getImageUrl(e.trim(), params))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Nullable
    public String getImageUrl(@NonNull final String imageKey, @Nullable final MediaParametersInput params) {
        final String resize;
        if (params != null) {
            checkArgument(params.getWidth() != null && params.getWidth() > 0, "Invalid width");
            checkArgument(params.getHeight() != null && params.getHeight() > 0, "Invalid height");

            resize = String.format(MEDIA_SERVICE_RESIZE_FORMAT, params.getWidth(), params.getHeight());
        } else {
            resize = null;
        }

        try {
            if (resize == null) {
                return feignMediaClient.getPublicUrl(MEDIA_SERVICE_IMG_PATH + imageKey);
            } else {
               return feignMediaClient.getPublicUrl(MEDIA_SERVICE_IMG_PATH + imageKey + resize);
            }
        } catch (final Exception ex) {
            log.warn("Cannot get upload url for key: {}", imageKey, ex);
            return null;
        }
    }
}
