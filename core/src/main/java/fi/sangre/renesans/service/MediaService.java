package fi.sangre.renesans.service;

import fi.sangre.media.rest.api.dto.UploadUrlRequestDto;
import fi.sangre.media.rest.api.dto.UploadUrlResponseDto;
import fi.sangre.renesans.application.client.FeignMediaClient;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.media.MediaType;
import fi.sangre.renesans.application.utils.MediaUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.graphql.input.media.MediaParametersInput;
import fi.sangre.renesans.graphql.input.media.MediaUploadInput;
import fi.sangre.renesans.graphql.output.media.MediaUploadOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor
@Slf4j

@Service
public class MediaService {
    private static final String PARAM = "p=";
    private static final String SLASH_ENCODED = "%2F";
    private static final String SLASH_DECODED = "/";
    private static final String PLUS_ENCODED = "%2B";
    private static final String PLUS_DECODED = "+";
    private static final String IMAGES_PATH_FORMAT = "%s/images";
    private static final String VIDEOS_PATH_FORMAT = "%s/videos";
    private static final String FILES_PATH_FORMAT = "%s/files";
    private static final String MEDIA_SERVICE_IMG_PATH = "img/";
    private static final String MEDIA_SERVICE_VID_PATH = "vid/";
    private static final String MEDIA_SERVICE_FILE_PATH = "file/";
    private static final String MEDIA_SERVICE_RESIZE_FORMAT = "+resize-w%dh%d";
    private final FeignMediaClient feignMediaClient;
    private final MediaUtils mediaUtils;

    @NonNull
    public MediaUploadOutput requestUploadUrl(@NonNull final SurveyId surveyId, @NonNull final MediaUploadInput input) {
        log.trace("Requesting upload url: input={}", input);

        final MediaType mediaType = mediaUtils.getTypeFromKey(input.getFileName());
        final String path;
        switch (mediaType) {
            case IMAGE:
                path = String.format(IMAGES_PATH_FORMAT, surveyId.asString());
                break;
            case VIDEO:
                path = String.format(VIDEOS_PATH_FORMAT, surveyId.asString());
                break;
            default:
                path = String.format(FILES_PATH_FORMAT, surveyId.asString());
        }

        try {
            final UploadUrlResponseDto response = feignMediaClient.requestUploadUrl(UploadUrlRequestDto.builder()
                    .filename(input.getFileName())
                    .mimeType(input.getMimeType())
                    .path(path)
                    .build());

            return MediaUploadOutput.builder()
                    .key(response.getKey())
                    .url(new URL(response.getUrl()))
                    .build();
        } catch (final Exception ex) {
            throw new InternalServiceException("Cannot get upload url from media service", ex);
        }
    }

    @NonNull
    public URL getMediaUrl(@NonNull final String imageKey, @Nullable final MediaParametersInput params) {
        final MediaType mediaType = mediaUtils.getTypeFromKey(imageKey);
        final String mediaServicePath;
        switch (mediaType) {
            case IMAGE:
                mediaServicePath = MEDIA_SERVICE_IMG_PATH;
                break;
            case VIDEO:
                mediaServicePath = MEDIA_SERVICE_VID_PATH;
                break;
            default:
                mediaServicePath = MEDIA_SERVICE_FILE_PATH;
        }

        final String resize;
        if (params != null && MediaType.IMAGE.equals(mediaType)) {
            checkArgument(params.getWidth() != null && params.getWidth() > 0, "Invalid width");
            checkArgument(params.getHeight() != null && params.getHeight() > 0, "Invalid height");

            resize = String.format(MEDIA_SERVICE_RESIZE_FORMAT, params.getWidth(), params.getHeight());
        } else {
            resize = null;
        }

        final String url;
        try {
            if (resize == null) {
                url = feignMediaClient.getPublicUrl(mediaServicePath + imageKey);
            } else {
                url = feignMediaClient.getPublicUrl(mediaServicePath + imageKey + resize);
            }
        } catch (final Exception ex) {
            log.warn("Cannot get upload url for key: {}", imageKey, ex);
            throw new InternalServiceException("Cannot get url", ex);
        }

        return Optional.ofNullable(url)
                .map(v -> StringUtils.replaceOnce(v, PARAM, StringUtils.EMPTY))
                .map(v -> StringUtils.replaceOnce(v, SLASH_ENCODED, SLASH_DECODED))
                .map(v -> StringUtils.replace(v, PLUS_ENCODED, PLUS_DECODED))
                .map(this::toUrl)
                .orElseThrow(() -> new InternalServiceException("Cannot get url"));
    }

    @Nullable
    private URL toUrl(@NonNull final String url) {
        try {
            return new URL(url);
        } catch (final MalformedURLException ex) {
            log.warn("Cannot transform to URL: {}", url, ex);
            return null;
        }
    }
}
