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

    private List<String> copyStringList(List<String> list) {
        if (list == null) {
            return null;
        }

        return new ArrayList<>(list);
    }
    private List<Long> copyLongList(List<Long> list) {
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

        this.setCountries(copyStringList(filters.getCountries()));
        this.setGenders(copyStringList(filters.getGenders()));
        this.setRespondentGroupIds(copyStringList(filters.getRespondentGroupIds()));
        this.setRespondentIds(copyStringList(filters.getRespondentIds()));

        this.setIndustryIds(copyLongList(filters.getIndustryIds()));
        this.setPositionIds(copyLongList(filters.getPositionIds()));
        this.setCustomerIds(copyLongList(filters.getCustomerIds()));
        this.setSegmentIds(copyLongList(filters.getSegmentIds()));
    }
}
