package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface ParameterChild {
    UUID getId();
    void setId(@NonNull UUID id);
    MultilingualText getLabel();
    void setLabel(@NonNull MultilingualText label);
}
