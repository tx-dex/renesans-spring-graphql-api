package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterMerger {

    @NonNull
    public List<Parameter> combine(@Nullable final List<Parameter> existing, @NonNull final List<Parameter> inputs) {
        final ImmutableList.Builder<Parameter> combined = ImmutableList.builder();
        final Map<UUID, Parameter> existingParameters;
        if (existing == null) {
            existingParameters = ImmutableMap.of();
        } else {
            existingParameters = existing.stream()
                    .collect(collectingAndThen(toMap(Parameter::getId, v -> v), Collections::unmodifiableMap));
        }

        for(final Parameter input : inputs) {
            if (input instanceof ListParameter) {
                combined.add(combine(existingParameters, (ListParameter) input));
            } else if (input instanceof TreeParameter) {
                combined.add(combine(existingParameters, (TreeParameter) input));
            } else {
                throw new SurveyException("Invalid input parameter type");
            }
        }

        return combined.build();
    }

    @NonNull
    private Parameter combine(@NonNull final Map<UUID, Parameter> existing, @NonNull final ListParameter input) {
        final UUID id;
        final Parameter combined;
        if (input.getId() == null) {
            id = UUID.randomUUID();
            combined = ListParameter.builder().id(id).build();
        } else {
            id = input.getId();
            combined = Optional.ofNullable(existing.get(id))
                    .orElse(ListParameter.builder().id(id).build());
        }

        if (combined instanceof ListParameter) {
            return combine((ListParameter) combined, input);
        } else {
            throw new SurveyException("Existing parameter has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private Parameter combine(@NonNull final ListParameter existing, @NonNull final ListParameter input) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel()));

        final ImmutableList.Builder<ParameterItem> values = ImmutableList.builder();
        final Map<UUID, ParameterItem> existingItems = Optional.ofNullable(existing.getValues())
                .orElse(ImmutableList.of())
                .stream()
                .collect(collectingAndThen(toMap(ParameterItem::getId, v -> v), Collections::unmodifiableMap));

        for (final ParameterItem item : input.getValues()) {
            values.add(combineItem(existingItems, item));
        }

        existing.setValues(values.build());

        return existing;
    }

    @NonNull
    private ParameterItem combineItem(@NonNull final Map<UUID, ParameterItem> existing, @NonNull final ParameterItem input) {
        final UUID id;
        final ParameterItem combined;
        if (input.getId() == null) {
            id = UUID.randomUUID();
            combined = ParameterItem.builder().id(id).build();
        } else {
            id = input.getId();
            combined = Optional.ofNullable(existing.get(id))
                    .orElse(ParameterItem.builder().id(id).build());
        }

        return combineItem(combined, input);
    }

    @NonNull
    private ParameterItem combineItem(@NonNull final ParameterItem existing, @NonNull final ParameterItem input) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel()));

        return existing;
    }

    @NonNull
    private Parameter combine(@NonNull final Map<UUID, Parameter> existing, @NonNull final TreeParameter input) {
        final UUID id;
        final Parameter combined;
        if (input.getId() == null) {
            id = UUID.randomUUID();
            combined = TreeParameter.builder().id(id).build();
        } else {
            id = input.getId();
            combined = Optional.ofNullable(existing.get(id))
                    .orElse(TreeParameter.builder().id(id).build());
        }

        if (combined instanceof TreeParameter) {
            return combine((TreeParameter) combined, input);
        } else {
            throw new SurveyException("Existing parameter has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private Parameter combine(@NonNull final TreeParameter existing, @NonNull final TreeParameter input) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel()));

        final ImmutableList.Builder<ParameterChild> children = ImmutableList.builder();
        final Map<UUID, ParameterChild> existingItems = Optional.ofNullable(existing.getChildren())
                .orElse(ImmutableList.of()).stream()
                .collect(collectingAndThen(toMap(ParameterChild::getId, v -> v), Collections::unmodifiableMap));

        for (final ParameterChild child : input.getChildren()) {
            children.add(combineChild(existingItems, child));
        }

        existing.setChildren(children.build());

        return existing;
    }

    @NonNull
    private ParameterChild combineChild(@NonNull final Map<UUID, ParameterChild> existing, @NonNull final ParameterChild input) {
        if (input instanceof ParameterItem) {
            final UUID id;
            final ParameterChild combined;
            if (input.getId() == null) {
                id = UUID.randomUUID();
                combined = ParameterItem.builder().id(id).build();
            } else {
                id = input.getId();
                combined = Optional.ofNullable(existing.get(id))
                        .orElse(ParameterItem.builder().id(id).build());
            }

            if (combined instanceof  ParameterItem) {
                return combineItem((ParameterItem) combined, (ParameterItem) input);
            } else {
                return combineItem(ParameterItem.builder().id(id).build(), (ParameterItem) input);
            }
        } else if (input instanceof TreeParameter)  {
            final UUID id;
            final ParameterChild combined;
            if (input.getId() == null) {
                id = UUID.randomUUID();
                combined = TreeParameter.builder().id(id).build();
            } else {
                id = input.getId();
                combined = Optional.ofNullable(existing.get(id))
                        .orElse(TreeParameter.builder().id(id).build());
            }

            if (combined instanceof  TreeParameter) {
                return (ParameterChild) combine((TreeParameter) combined, (TreeParameter) input);
            } else {
                return (ParameterChild) combine(TreeParameter.builder().id(id).build(), (TreeParameter) input);
            }
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }
}
