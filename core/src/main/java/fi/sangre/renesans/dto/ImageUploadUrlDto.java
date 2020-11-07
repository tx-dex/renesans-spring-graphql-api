package fi.sangre.renesans.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ImageUploadUrlDto {
    private String hash;
    private String url;
}
