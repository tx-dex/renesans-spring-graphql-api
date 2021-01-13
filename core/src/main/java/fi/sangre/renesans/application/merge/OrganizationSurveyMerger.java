package fi.sangre.renesans.application.merge;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.media.MediaDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyMerger {
    private final StaticTextMerger staticTextMerger;
    private final ParameterMerger parameterMerger;
    private final CatalystMerger catalystMerger;
    private final MediaMerger mediaMerger;

    @NonNull
    public OrganizationSurvey combine(@NonNull final OrganizationSurvey existing, @NonNull final OrganizationSurvey input) {
        //TODO: check version here
        existing.setVersion(input.getVersion());
        existing.setLogo(combine(existing.getLogo(), input.getLogo()));
        existing.setMedia(mediaMerger.combine(existing.getMedia(), input.getMedia()));
        existing.setCatalysts(catalystMerger.combine(existing.getCatalysts(), input.getCatalysts()));
        existing.setParameters(parameterMerger.combine(existing.getParameters(), input.getParameters()));
        existing.setStaticTexts(staticTextMerger.combine(existing.getStaticTexts(), input.getStaticTexts()));

        return existing;
    }

    @Nullable
    private MediaDetails combine(@Nullable final MediaDetails existing, @Nullable final MediaDetails input) {
        if (input == null) {
            return existing;
        } else {
            if (StringUtils.trimToNull(input.getKey()) == null) {
                return null;
            } else {
                return MediaDetails.builder()
                        .key(StringUtils.trim(input.getKey()))
                        .build();
            }
        }
    }
}
