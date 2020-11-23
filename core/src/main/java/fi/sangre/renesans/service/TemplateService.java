package fi.sangre.renesans.service;

import fi.sangre.renesans.application.assemble.SurveyTemplateAssembler;
import fi.sangre.renesans.application.model.SurveyTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Service
public class TemplateService {
    private final SegmentService segmentService;
    private final SurveyTemplateAssembler surveyTemplateAssembler;

    @NonNull
    public List<SurveyTemplate> getTemplates(@NonNull final String languageTag) {
        return segmentService.getAllSegments()
                .stream()
                .map(e -> surveyTemplateAssembler.fromSegment(e, languageTag))
                .sorted((e1,e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
