package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.application.model.TemplateId;
import fi.sangre.renesans.model.Segment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyTemplateAssembler {

    @NonNull
    public SurveyTemplate fromSegment(@NonNull final Segment segment, @NonNull final String languageTag) {
        return SurveyTemplate.builder()
                .id(new TemplateId(segment.getId()))
                .version(1L) //TODO: implement
                .titles(new MultilingualText(ImmutableMap.of(languageTag, segment.getName())))
                .descriptions(new MultilingualText(ImmutableMap.of()))
                .build();
    }
}
