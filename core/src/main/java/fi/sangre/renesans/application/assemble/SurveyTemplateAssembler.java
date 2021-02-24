package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.application.model.TemplateId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.model.Segment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Deprecated
@Component
public class SurveyTemplateAssembler {
    private final MultilingualUtils multilingualUtils;
    @NonNull
    public SurveyTemplate fromSegment(@NonNull final Segment segment, @NonNull final String languageTag) {
        return SurveyTemplate.builder()
                .id(new TemplateId(segment.getId()))
                .version(1L) //TODO: implement
                .titles(multilingualUtils.create(segment.getName(), languageTag))
                .descriptions(multilingualUtils.empty())
                .build();
    }
}
