package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AppMutations implements GraphQLMutationResolver {
    private final QuestionnaireFacade questionnaireFacade;

    // NOTICE!!!
    // this is public and respondent does not have token for that yet. Do not authorize it!!!
    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final UUID id, @NonNull final String invitationHash) {
        return questionnaireFacade.openQuestionnaire(id, invitationHash);
    }
}
