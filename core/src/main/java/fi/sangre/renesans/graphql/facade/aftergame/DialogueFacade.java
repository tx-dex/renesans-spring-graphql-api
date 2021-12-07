package fi.sangre.renesans.graphql.facade.aftergame;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.graphql.output.dialogue.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueFacade {
    private final AfterGameFacade afterGameFacade;

    @NonNull
    public DialogueTotalStatisticsOutput getDialogueTotalStatistics(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = afterGameFacade.getSurvey(questionnaireId, principal);

        return getFakeTotalStatistics();
    }

    public Collection<DialogueTopicOutput> getDialogueTopics(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = afterGameFacade.getSurvey(questionnaireId, principal);

        List<DialogueTopicOutput> topics = new ArrayList<>();
        topics.add(getFakeTopic());
        topics.add(getFakeTopic());
        topics.add(getFakeTopic());

        return topics;
    }

    public DialogueTopicOutput getDialogueTopic(
            @NonNull final UUID questionnaireId,
            @NonNull final UUID topicId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = afterGameFacade.getSurvey(questionnaireId, principal);

        return getFakeTopic();
    }

    private DialogueTopicOutput getFakeTopic() {
        DialogueCommentOutput comment1Reply = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("Ei, see on vana")
                .likesCount(0)
                .hasLikeByThisRespondent(false)
                .createdAt("2021-11-20T11:03:51.612Z")
                .respondentColor("#00ff00")
                .build();

        DialogueCommentOutput comment1 = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("Kas me seda küsimust arutame?")
                .likesCount(2)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:00:00.612Z")
                .respondentColor("#ee0000")
                .build();


        DialogueCommentOutput comment2 = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("O hi!")
                .likesCount(1)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:30:00.612Z")
                .respondentColor("#ee0000")
                .build();


        List<DialogueCommentOutput> commentsList1 = Arrays.asList(comment1, comment2);
        DialogueQuestionOutput question1 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("First question title")
                .active(true)
                .sortOrder(1)
                .answersCount(3)
                .likesCount(3)
                .hasLikeByThisRespondent(true)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question2 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Another question title")
                .active(true)
                .sortOrder(2)
                .answersCount(2)
                .likesCount(2)
                .hasLikeByThisRespondent(false)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question3 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Closed question title")
                .active(false)
                .sortOrder(2)
                .answersCount(1)
                .likesCount(1)
                .hasLikeByThisRespondent(false)
                // don't send any comments since the question is archived
                .comments(Collections.emptyList())
                .build();

        DialogueTipOutput tip1 = DialogueTipOutput.builder().id(UUID.randomUUID()).text("Some tip will be here").build();
        DialogueTipOutput tip2 = DialogueTipOutput.builder().id(UUID.randomUUID()).text("Lorem ipsum sit amet").build();

        return DialogueTopicOutput.builder()
                .id(UUID.randomUUID())
                .title("Topic #1")
                .active(true)
                .questionsCount(3)
                .tips(Arrays.asList(tip1, tip2))
                .questions(Arrays.asList(question1, question2, question3))
                .sortOrder(1)
                .build();
    }

    private DialogueTotalStatisticsOutput getFakeTotalStatistics() {
        return DialogueTotalStatisticsOutput.builder()
                .id(UUID.randomUUID())
                .activeTopicsCount(3)
                .likesCount(5)
                .answersCount(3)
                .build();
    }
}
