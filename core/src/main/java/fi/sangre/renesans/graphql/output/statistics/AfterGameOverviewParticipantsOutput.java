package fi.sangre.renesans.graphql.output.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AfterGameOverviewParticipantsOutput {
    private UUID id;
    private Long participantsCount;
    private Long invitedParticipantsCount;
    private Long engagementRatio;
}
