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

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterUtils {
    @NonNull
    public List<ParameterChild> getChildren(@NonNull final List<Parameter> parameters) {
        return parameters.stream()
                .map(this::getChildren)
                .flatMap(Collection::stream)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<ParameterChild> getChildren(@NonNull final Parameter parameter) {
        if (parameter instanceof ParentParameter) {
            final ParentParameter parent = (ParentParameter) parameter;
            return ImmutableList.copyOf(
                    Optional.of(parent.getLeaves().stream().map(child -> (ParameterChild) child).collect(toList()))
                    .orElse(ImmutableList.of()));
        } else if (parameter instanceof ListParameter) {
            final ListParameter list = (ListParameter) parameter;
            return ImmutableList.copyOf(Optional.ofNullable(list.getValues())
                    .orElse(ImmutableList.of()));
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    public List<Parameter> getAllChildren(@NonNull final List<Parameter> parameters) {
        return parameters.stream()
                .map(this::getAllChildren)
                .flatMap(Collection::stream)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<Parameter> getAllChildren(@NonNull final Parameter parameter) {
        if (parameter instanceof ParentParameter) {
            final ParentParameter parent = (ParentParameter) parameter;
            return ImmutableList.copyOf(Optional.ofNullable(parent.getAllChildren())
                    .orElse(ImmutableList.of()));
        } else if (parameter instanceof ListParameter) {
            final ListParameter list = (ListParameter) parameter;
            return ImmutableList.copyOf(Optional.ofNullable(list.getValues())
                    .orElse(ImmutableList.of()));
        } else {
            return new ArrayList<>();
        }
    }

    @Nullable
    public ParameterChild findChild(@NonNull final ParameterId childId, @NonNull final Parameter parameter) {
        if (parameter instanceof ParentParameter) {
            final ParentParameter parent = (ParentParameter) parameter;
            if (parent.hasChildren()) {
                return (ParameterChild) parent.getLeaves().stream()
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

    @NonNull
    public List<ParameterChild> findChildren(@Nullable final Set<ParameterId> childrenIds, @Nullable final Parameter parameter) {
        if (parameter == null || childrenIds == null) {
            return ImmutableList.of();
        } else {
            if (parameter instanceof ParentParameter) {
                final ParentParameter parent = (ParentParameter) parameter;
                if (parent.hasChildren()) {
                    return parent.getLeaves().stream()
                            .filter(child -> childrenIds.contains(child.getId()))
                            .map(child -> (ParameterChild) child)
                            .collect(collectingAndThen(toList(), Collections::unmodifiableList));
                } else {
                    return ImmutableList.of();
                }
            } else if (parameter instanceof ListParameter) {
                final ListParameter list = (ListParameter) parameter;
                return Optional.ofNullable(list.getValues())
                        .orElse(ImmutableList.of())
                        .stream()
                        .filter(child -> childrenIds.contains(child.getId()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            } else {
                throw new SurveyException("Invalid parameter type");
            }
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

    public List<Parameter> getAllParents(@NonNull final Parameter parameter) {
        List<Parameter> parents = new ArrayList<>();
        Parameter parentParameter = getParent(parameter);
        if (parentParameter != null) {
            parents.add(parentParameter);
            parents.addAll(getAllParents(parentParameter));
        }
        return parents;
    }
}
