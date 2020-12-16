package fi.sangre.renesans.persistence.model;

import fi.sangre.renesans.application.model.OrganizationId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
public class SurveyStateCounters {
   private final OrganizationId id;
   private final long all;

   public SurveyStateCounters(final UUID id, final long all) {
       this.id = new OrganizationId(id);
       this.all = all;
   }
}
