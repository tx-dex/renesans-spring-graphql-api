package fi.sangre.renesans.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientDto {
    private String groupId;
    private String email;
    private String hash;
    private String status;
    private String errorMessage;
    private Map<String, Object> parameters;
}
