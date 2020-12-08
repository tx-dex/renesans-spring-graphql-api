package fi.sangre.renesans.graphql.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StaticTextGroupOutput {
    private String id;
    private String title;
    private String description;
    private List<StaticTextOutput> texts;
}
