package fi.sangre.renesans.application.merge;

import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationSurveyMerger {
    private final SurveyRepository surveyRepository;
    private final StaticTextMerger staticTextMerger;
    private final ParameterMerger parameterMerger;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey combine(@NonNull final OrganizationSurvey input) {
        final OrganizationSurvey existing = surveyRepository.findById(input.getId())
                .map(organizationSurveyAssembler::from)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

        //TODO: check version here
        existing.setVersion(input.getVersion());
        existing.setParameters(parameterMerger.combine(existing.getParameters(), input.getParameters()));
        existing.setStaticTexts(staticTextMerger.combine(existing.getStaticTexts(), input.getStaticTexts()));


        return existing;
    }
}
