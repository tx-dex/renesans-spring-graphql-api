package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.CatalystId;

public interface CatalystOutput {
    CatalystId getId();
    String getPdfName();
}
