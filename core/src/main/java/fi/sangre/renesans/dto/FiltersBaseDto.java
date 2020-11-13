package fi.sangre.renesans.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiltersBaseDto {
    private UUID surveyId;
    private String respondentName;
    private Long ageMin = 0L;
    private Long ageMax = 100L;
    private List<String> countries;
    private Long experienceMin = 0L;
    private Long experienceMax = 100L;
    private List<String> genders;
    private List<Long> industryIds;
    private List<Long> positionIds;
    private List<UUID> customerIds;
    private List<String> respondentGroupIds;
    private List<String> respondentIds;
    private List<Long> segmentIds;

    public boolean hasActiveProfileFilters() {
        return  (genders != null && genders.size() > 0) ||
                respondentName != null ||
                getAgeMin() != null ||
                getAgeMax() != null ||
                (countries != null && countries.size() > 0) ||
                getExperienceMin() != null ||
                getExperienceMax() != null ||
                (segmentIds != null && segmentIds.size() > 0) ||
                (positionIds != null && positionIds.size() > 0) ||
                (industryIds != null && industryIds.size() > 0);
    }

    public Long getAgeMin() {
        return ageMin != null && ageMin != 0L ? ageMin : null;
    }
    public Long getAgeMax() {
        return ageMax != null && ageMax != 100L ? ageMax : null;
    }
    public Long getExperienceMin() {
        return experienceMin != null && experienceMin != 0L ? experienceMin : null;
    }
    public Long getExperienceMax() {
        return experienceMax != null && experienceMax != 100L ? experienceMax : null;
    }
}
