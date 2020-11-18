package fi.sangre.renesans.persistence.model.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MultilingualMetadata implements Serializable {
    private Map<String, String> phrases;
}
