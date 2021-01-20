package fi.sangre.renesans.application.merge;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.UUIDUtils;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static fi.sangre.renesans.application.model.ParameterId.GLOBAL_EVERYONE_PARAMETER_ID;
import static fi.sangre.renesans.application.model.ParameterId.GLOBAL_YOU_PARAMETER_ID;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterMerger {
    private static final Set<ParameterId> GLOBAL_PARAMETER_IDS = ImmutableSet.of(
            GLOBAL_YOU_PARAMETER_ID,
            GLOBAL_EVERYONE_PARAMETER_ID);


    private final MultilingualUtils multilingualUtils;
    private final UUIDUtils uuidUtils;

    @NonNull
    public List<Parameter> combine(@NonNull final List<Parameter> existing, @Nullable final List<Parameter> inputs) {
        if (inputs == null) {
            log.trace("Copied parameters from existing. {}", existing);
            return ImmutableList.copyOf(existing);
        } else {
            final List<Parameter> combined = new LinkedList<>();
            final Map<ParameterId, Parameter> existingParameters = existing.stream()
                        .collect(collectingAndThen(toMap(Parameter::getId, v -> v), Collections::unmodifiableMap));

            for (final Parameter input : inputs) {
                if (input instanceof ListParameter) {
                    combined.add(combine(existingParameters, (ListParameter) input));
                } else if (input instanceof TreeParameter) {
                    combined.add(combine(existingParameters, (TreeParameter) input));
                } else {
                    throw new SurveyException("Invalid input parameter type");
                }
            }

            log.trace("Combined parameters: {}", combined);

            validate(combined);

            return Collections.unmodifiableList(combined);
        }
    }

    private void validate(@NonNull final List<Parameter> parameters) {
        multilingualUtils.checkDuplicates(parameters.stream()
                        .map(Parameter::getLabel)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)),
                "Duplicate parameter name"
        );

        // TODO: check children labels?
    }

    @NonNull
    private Parameter combine(@NonNull final Map<ParameterId, Parameter> existing, @NonNull final ListParameter input) {
        final ParameterId id = createParameterId(input.getId());
        final Parameter combined = Optional.ofNullable(existing.get(id))
                .orElse(ListParameter.builder().id(id).build());


        if (combined instanceof ListParameter) {
            return combine((ListParameter) combined, input);
        } else {
            throw new SurveyException("Existing parameter has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private Parameter combine(@NonNull final ListParameter existing, @NonNull final ListParameter input) {
        existing.setLabel(multilingualUtils.combine(existing.getLabel(), input.getLabel()));

        final ImmutableList.Builder<ParameterItem> values = ImmutableList.builder();
        final Map<ParameterId, ParameterItem> existingItems = Optional.ofNullable(existing.getValues())
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
    private ParameterItem combineItem(@NonNull final Map<ParameterId, ParameterItem> existing, @NonNull final ParameterItem input) {
        final ParameterId id = createParameterId(input.getId());
        final ParameterItem combined = Optional.ofNullable(existing.get(id))
                .orElse(ParameterItem.builder().id(id).build());


        return combineItem(combined, input);
    }

    @NonNull
    private ParameterItem combineItem(@NonNull final ParameterItem existing, @NonNull final ParameterItem input) {
        existing.setLabel(multilingualUtils.combine(existing.getLabel(), input.getLabel()));

        return existing;
    }

    @NonNull
    private Parameter combine(@NonNull final Map<ParameterId, Parameter> existing, @NonNull final TreeParameter input) {
        final ParameterId id = createParameterId(input.getId());
        final Parameter combined = Optional.ofNullable(existing.get(id))
                .orElse(TreeParameter.builder().id(id).build());

        if (combined instanceof TreeParameter) {
            return combine((TreeParameter) combined, input);
        } else {
            throw new SurveyException("Existing parameter has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private Parameter combine(@NonNull final TreeParameter existing, @NonNull final TreeParameter input) {
        existing.setLabel(multilingualUtils.combine(existing.getLabel(), input.getLabel()));

        final ImmutableList.Builder<ParameterChild> children = ImmutableList.builder();
        final Map<ParameterId, ParameterChild> existingItems = Optional.ofNullable(existing.getChildren())
                .orElse(ImmutableList.of()).stream()
                .collect(collectingAndThen(toMap(ParameterChild::getId, v -> v), Collections::unmodifiableMap));

        for (final ParameterChild child : input.getChildren()) {
            children.add(combineChild(existingItems, child));
        }

        existing.setChildren(children.build());

        return existing;
    }

    @NonNull
    private ParameterChild combineChild(@NonNull final Map<ParameterId, ParameterChild> existing, @NonNull final ParameterChild input) {
        if (input instanceof ParameterItem) {
            final ParameterId id = createParameterId(input.getId());
            final ParameterChild combined = Optional.ofNullable(existing.get(id))
                    .orElse(ParameterItem.builder().id(id).build());


            if (combined instanceof  ParameterItem) {
                return combineItem((ParameterItem) combined, (ParameterItem) input);
            } else {
                return combineItem(ParameterItem.builder().id(id).build(), (ParameterItem) input);
            }
        } else if (input instanceof TreeParameter)  {
            final ParameterId id = createParameterId(input.getId());
            final ParameterChild combined = Optional.ofNullable(existing.get(id))
                    .orElse(TreeParameter.builder().id(id).build());


            if (combined instanceof  TreeParameter) {
                return (ParameterChild) combine((TreeParameter) combined, (TreeParameter) input);
            } else {
                return (ParameterChild) combine(TreeParameter.builder().id(id).build(), (TreeParameter) input);
            }
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private ParameterId createParameterId(@Nullable final ParameterId id) {
        return Optional.ofNullable(id)
                .map(ParameterId::new)
                .orElseGet(this::generateParameterId);
    }

    @NonNull
    private ParameterId generateParameterId() {
        ParameterId newId = new ParameterId(UUID.randomUUID());
        while (GLOBAL_PARAMETER_IDS.contains(newId)) {
            newId = new ParameterId(UUID.randomUUID());
        }

        return newId;
    }
}
