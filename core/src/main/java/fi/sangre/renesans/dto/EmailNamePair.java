package fi.sangre.renesans.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data

public class EmailNamePair {
    private String email;
    private String name;
}
