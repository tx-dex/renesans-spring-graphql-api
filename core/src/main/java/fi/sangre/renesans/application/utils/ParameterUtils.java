package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.parameter.ListParameter;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.parameter.ParentParameter;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterUtils {
    @Nullable
    public ParameterChild findChild(@NonNull final ParameterId childId, @NonNull final Parameter parameter) {
        if (parameter instanceof ParentParameter) {
            final ParentParameter parent = (ParentParameter) parameter;
            if (parent.hasChildren()) {
                return parent.getLeaves().stream()
                        .filter(child -> childId.equals(child.getId()))
                        .findFirst()
                        .orElse(null);
            } else {
                return null;
            }
        } else if (parameter instanceof ListParameter) {
            final ListParameter list = (ListParameter) parameter;
            return Optional.ofNullable(list.getValues())
                    .orElse(ImmutableList.of())
                    .stream()
                    .filter(value -> childId.equals(value.getId()))
                    .findFirst()
                    .orElse(null);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @Nullable
    public Parameter getParent(@NonNull final Parameter parameter) {
        if (parameter instanceof ParameterChild) {
            return ((ParameterChild) parameter).getParent();
        } else {
            return null;
        }
    }
}
