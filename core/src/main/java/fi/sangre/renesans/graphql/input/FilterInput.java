package fi.sangre.renesans.graphql.input;

import lombok.Data;

import java.util.List;

@Data
public class FilterInput {
    private String id;
    private List<String> values;
}
