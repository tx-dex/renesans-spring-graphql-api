package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.media.MediaType;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j

@Component
public class MediaUtils {
    private static final String DOT = ".";
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = ImmutableSet.of("png", "jpg", "jpeg", "gif");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = ImmutableSet.of("mp4", "webc", "mov");
    private static final Set<String> ALLOWED_PDF_EXTENSIONS = ImmutableSet.of("pdf");

    public MediaType getTypeFromKey(@NonNull final String key) {
        final String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(key, DOT));

        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return MediaType.IMAGE;
        } else if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return MediaType.VIDEO;
        } else if (ALLOWED_PDF_EXTENSIONS.contains(extension)) {
            return MediaType.PDF;
        } else {
            throw new SurveyException("Invalid image type");
        }
    }
}
