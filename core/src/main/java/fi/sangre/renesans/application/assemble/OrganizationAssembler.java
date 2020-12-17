package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.persistence.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationAssembler {
    @NonNull
    public Organization from(@NonNull final Customer entity) {
        return Organization.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .owner(entity.getOwner())
                .build();
    }
}
