package fi.sangre.renesans.graphql.output.question;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

@Data
@ToString

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionDriverWeights {
    private final Map<String, Double> weights;

    public QuestionDriverWeights(final Map<String, Double> weights) {
        this.weights = Objects.requireNonNull(weights, "Weight map must not be null");
    }

    @JsonAnyGetter
    public Map<String, Double> getWeights() {
        return weights;
    }
}
