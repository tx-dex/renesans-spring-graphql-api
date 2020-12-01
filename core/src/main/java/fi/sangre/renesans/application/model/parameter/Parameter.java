package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;
import org.springframework.lang.NonNull;

public interface Parameter {
    ParameterId getId();
    void setId(@NonNull ParameterId id);
    MultilingualText getLabel();
    void setLabel(@NonNull MultilingualText label);


}
