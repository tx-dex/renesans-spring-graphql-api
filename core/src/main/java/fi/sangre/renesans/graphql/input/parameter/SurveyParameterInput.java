package fi.sangre.renesans.graphql.input.parameter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SurveyListParameterInput.class, name = "LIST"),
        @JsonSubTypes.Type(value = SurveyTreeParameterInput.class, name = "TREE"),
})
public interface SurveyParameterInput {

}
