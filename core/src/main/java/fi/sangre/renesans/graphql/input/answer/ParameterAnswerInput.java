package fi.sangre.renesans.graphql.input.answer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ParameterAnswerInput {
    private UUID parameterId;
    private UUID value;
}

