package fi.sangre.renesans.application.model;

import com.google.common.collect.ImmutableMap;
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
    public static final Map<String, MultilingualText> EMPTY = ImmutableMap.of();

    private Map<String, MultilingualText> texts;
}
