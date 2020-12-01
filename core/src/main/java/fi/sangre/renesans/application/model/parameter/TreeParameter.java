package fi.sangre.renesans.application.model.parameter;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"root", "parent"})
@Builder
public class TreeParameter implements Parameter, ParentParameter, ParameterChild {
    private Parameter root;
    private Parameter parent;
    private ParameterId id;
    private MultilingualText label;
    private List<ParameterChild> children;

    @Override
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Override
    public List<ParameterChild> getLeaves() {
        if (hasChildren()) {
            final ImmutableList.Builder<ParameterChild> children = ImmutableList.builder();
            for(final ParameterChild child : this.children) {
                if (child instanceof ParentParameter) {
                    children.addAll(((ParentParameter) child).getLeaves());
                } else {
                    children.add(child);
                }
            }
            return children.build();
        } else {
            return ImmutableList.of();
        }
    }


}
