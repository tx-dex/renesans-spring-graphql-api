package fi.sangre.renesans.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class DriverInput {
    private Long id;
    private String title;
    private String description;
    private String prescription;
    private Double weight;
}
