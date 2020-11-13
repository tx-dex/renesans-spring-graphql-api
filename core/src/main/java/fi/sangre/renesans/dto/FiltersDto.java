package fi.sangre.renesans.dto;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeVisitor;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class FiltersDto extends FiltersBaseDto implements GraphQLInputType {
    @Override
    public String getName() {
        return "Filters";
    }

    @Override
    public TraversalControl accept(TraverserContext<GraphQLType> context, GraphQLTypeVisitor visitor) {
        return null;
    }

    private <T> List<T> copyList(List<T> list) {
        if (list == null) {
            return null;
        }

        return new ArrayList<>(list);
    }

    public FiltersDto(FiltersDto filters) {
        this.setSurveyId(filters.getSurveyId());

        this.setSurveyId(filters.getSurveyId());
        this.setRespondentName(filters.getRespondentName());
        this.setAgeMin(filters.getAgeMin());
        this.setAgeMax(filters.getAgeMax());
        this.setExperienceMin(filters.getExperienceMin());
        this.setExperienceMax(filters.getExperienceMax());

        this.setCountries(copyList(filters.getCountries()));
        this.setGenders(copyList(filters.getGenders()));
        this.setRespondentGroupIds(copyList(filters.getRespondentGroupIds()));
        this.setRespondentIds(copyList(filters.getRespondentIds()));

        this.setIndustryIds(copyList(filters.getIndustryIds()));
        this.setPositionIds(copyList(filters.getPositionIds()));
        this.setCustomerIds(copyList(filters.getCustomerIds()));
        this.setSegmentIds(copyList(filters.getSegmentIds()));
    }
}
