package fi.sangre.renesans.application.model.parameter;

public interface ParameterChild extends Parameter {
    Parameter getParent();
    void setParent(Parameter parent);
    Parameter getRoot();
    void setRoot(Parameter root);
}
