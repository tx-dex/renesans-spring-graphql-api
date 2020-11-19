package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.questions.LikertQuestion;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = {"question", "weight"})
@Builder
public class DriverWeight {
    public static final Double DEFAULT_WEIGHT = 0d;

    private LikertQuestion question;
    private Driver driver;
    @Builder.Default
    private Double weight = DEFAULT_WEIGHT;
}
