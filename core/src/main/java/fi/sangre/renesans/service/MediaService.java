package fi.sangre.renesans.service;

import fi.sangre.media.rest.api.dto.SignedUrlResponseDto;
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor
@Slf4j

@Service
public class MediaService {
    private static final String PARAM = "p=";
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
    public URL getPublicUrl(@NonNull final String key, @Nullable final MediaParametersInput params) {
        final MediaType mediaType = mediaUtils.getTypeFromKey(key);
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

        final String resize = getResizeParameters(mediaType, params);

        final String url;
        try {
            if (resize == null) {
                url = feignMediaClient.getPublicUrl(mediaServicePath + key);
            } else {
                url = feignMediaClient.getPublicUrl(mediaServicePath + key + resize);
            }
        } catch (final Exception ex) {
            log.warn("Cannot get upload url for key: {}", key, ex);
            throw new InternalServiceException("Cannot get url", ex);
        }

        try {
            if (url == null) { // Actually client may return null
                throw new InternalServiceException("Cannot convert empty url");
            }

            return toDecodedUrl(StringUtils.replaceOnce(url, PARAM, StringUtils.EMPTY));
        } catch (final MalformedURLException | UnsupportedEncodingException ex) {
            log.warn("Cannot convert url");
            throw new InternalServiceException("Cannot convert url", ex);
        }
    }

    @NonNull
    public URL getSignedUrl(@NonNull final String key, @Nullable final MediaParametersInput params) {
        final MediaType mediaType = mediaUtils.getTypeFromKey(key);

        final String resize = getResizeParameters(mediaType, params);

        final Optional<SignedUrlResponseDto> dto;
        try {
            if (resize == null) {
                dto = Optional.ofNullable(feignMediaClient.getSignedUrl(key));
            } else {
                dto = Optional.ofNullable(feignMediaClient.getSignedUrl(key + resize));
            }
        } catch (final Exception ex) {
            log.warn("Cannot get upload url for key: {}", key, ex);
            throw new InternalServiceException("Cannot get url", ex);
        }

        final String url = dto
                .map(SignedUrlResponseDto::getUrl)
                .map(StringUtils::trimToNull)
               .orElseThrow(() -> new InternalServiceException("Cannot convert empty url"));
        try {
            return new URL(url);
        } catch (final MalformedURLException ex) {
            log.warn("Cannot convert url");
            throw new InternalServiceException("Cannot convert url", ex);

        }
    }

    @NonNull
    private URL toDecodedUrl(@NonNull final String url) throws UnsupportedEncodingException, MalformedURLException {
        return new URL(URLDecoder.decode(url, "UTF-8"));
    }

    @Nullable
    private String getResizeParameters(@NonNull final MediaType type, @Nullable final MediaParametersInput params) {
        final String resize;
        if (params != null && MediaType.IMAGE.equals(type)) {
            checkArgument(params.getWidth() != null && params.getWidth() > 0, "Invalid width");
            checkArgument(params.getHeight() != null && params.getHeight() > 0, "Invalid height");

            resize = String.format(MEDIA_SERVICE_RESIZE_FORMAT, params.getWidth(), params.getHeight());
        } else {
            resize = null;
        }

        return resize;
    }
}
