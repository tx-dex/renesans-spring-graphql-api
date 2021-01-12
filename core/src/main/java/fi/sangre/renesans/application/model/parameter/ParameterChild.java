package fi.sangre.renesans.application.model.parameter;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;

public interface ParameterChild extends Parameter {
    ParameterId getId();
    MultilingualText getLabel();
    Parameter getParent();
    void setParent(Parameter parent);
    Parameter getRoot();
    void setRoot(Parameter root);
}
