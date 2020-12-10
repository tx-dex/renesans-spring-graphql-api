package fi.sangre.renesans.application.model;

import java.io.Serializable;

public interface IdValueObject<T> extends Serializable {
    T getValue();
    String asString();
}
