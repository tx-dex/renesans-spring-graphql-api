package fi.sangre.renesans.service;

import fi.sangre.renesans.exception.SurveyNotFoundException;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor

@Service
@Transactional
public class SurveyService {
    private final CustomerService customerService;
    private final SurveyRepository surveyRepository;

    public Survey getDefaultSurvey() {
        return surveyRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new SurveyNotFoundException("Default"));
    }

    public Survey getSurvey(final String id, final Long customerId) {
        //TODO: should never return null?
        final Survey survey = surveyRepository.findById(id).orElse(null);
        if (survey != null && customerId != null) {
            survey.setSegment(customerService.getCustomer(customerId).getSegment());
        }

        return survey;
    }

    public Survey getSurvey(RespondentGroup respondentGroup) {
        return surveyRepository.findByRespondentGroupsContaining(respondentGroup);
    }
}
