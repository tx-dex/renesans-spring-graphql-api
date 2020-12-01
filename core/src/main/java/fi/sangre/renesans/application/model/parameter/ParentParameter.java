package fi.sangre.renesans.application.model.parameter;

import org.springframework.lang.NonNull;

import java.util.List;

public interface ParentParameter {
    List<ParameterChild> getChildren();
    void setChildren(@NonNull List<ParameterChild> children);
    boolean hasChildren();
    List<ParameterChild> getLeaves();

}
