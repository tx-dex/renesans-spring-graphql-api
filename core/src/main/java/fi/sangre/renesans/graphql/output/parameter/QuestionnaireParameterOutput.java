package fi.sangre.renesans.graphql.output.parameter;

import org.springframework.lang.NonNull;

import java.util.Set;
import java.util.UUID;

public interface QuestionnaireParameterOutput {
    void setSelectedAnswer(@NonNull Set<UUID> selectedAnswer);
}
