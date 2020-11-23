package fi.sangre.renesans.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DriverInput {
    private Long id;
    private String title;
    private String description;
    private String prescription;
    private Double weight;
}
