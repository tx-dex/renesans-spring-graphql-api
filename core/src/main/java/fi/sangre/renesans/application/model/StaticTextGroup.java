package fi.sangre.renesans.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StaticTextGroup {
    private Map<String, MultilingualText> texts;
}
