package fi.sangre.renesans.service;

import com.querydsl.core.BooleanBuilder;
import fi.sangre.renesans.dto.FiltersDto;
import fi.sangre.renesans.model.QRespondent;
import fi.sangre.renesans.model.Respondent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FilterService {
    private Boolean hasArrayParameter(List list) {
        return list != null && !list.isEmpty();
    }

    public BooleanBuilder apply(FiltersDto filters, QRespondent respondent) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(respondent.archived.eq(false));
        builder.and(respondent.state.eq(Respondent.State.FINISHED));

        if (filters == null) {
            return builder;
        }

        UUID surveyId = filters.getSurveyId();
        Long ageMax = filters.getAgeMax();
        Long ageMin = filters.getAgeMin();
        List<String> countries = filters.getCountries();
        Long experienceMax = filters.getExperienceMax();
        Long experienceMin = filters.getExperienceMin();
        List<String> genders = filters.getGenders();
        List<Long> industryIds = filters.getIndustryIds();
        List<Long> positionIds = filters.getPositionIds();
        List<UUID> customerIds = filters.getCustomerIds();
        List<String> respondentGroupIds = filters.getRespondentGroupIds();
        List<String> respondentIds = filters.getRespondentIds();
        List<Long> segmentIds = filters.getSegmentIds();

        if (surveyId != null) {
            builder.and(respondent
                    .respondentGroup
                    .survey
                    .id
                    .eq(surveyId));
        }

        if (ageMin != null || ageMax != null) {
            builder.and(respondent.age.between(ageMin, ageMax));
        }

        if (this.hasArrayParameter(countries)) {
            builder.and(respondent.country.in(countries));
        }

        if (experienceMin != null || experienceMax != null) {
            builder.and(respondent.experience.between(experienceMin, experienceMax));
        }

        if (this.hasArrayParameter(genders)) {
            builder.and(respondent.gender.in(genders));
        }

        if (this.hasArrayParameter(industryIds)) {
            builder.and(respondent.industry.id.in(industryIds));
        }

        if (this.hasArrayParameter(positionIds)) {
            builder.and(respondent.position.id.in(positionIds));
        }

        if (this.hasArrayParameter(segmentIds)) {
            builder.and(respondent.segment.id.in(segmentIds));
        }

        if (this.hasArrayParameter(customerIds)) {
            builder.and(respondent.respondentGroup.customer.id.in(customerIds));
        }

        if (this.hasArrayParameter(respondentGroupIds)) {
            builder.and(respondent.respondentGroup.id.in(respondentGroupIds));
        }

        if (this.hasArrayParameter(respondentIds)) {
            builder.and(respondent.id.in(respondentIds));
        }

        return builder;
    }
}
