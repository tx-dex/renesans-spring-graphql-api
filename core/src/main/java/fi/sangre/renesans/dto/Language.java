package fi.sangre.renesans.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class Language {
    private String name;
    private String nativeName;
    private String prompt;
    private String code;
}
