package fi.sangre.renesans.persistence.model.metadata;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class SurveyMetadata implements Serializable {
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private List<CatalystMetadata> catalysts;
}
