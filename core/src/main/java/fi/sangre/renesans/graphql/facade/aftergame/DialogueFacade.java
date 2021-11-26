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
                .text("Ei, see on vana")
                .likesCount(0)
                .hasLikeByThisRespondent(false)
                .createdAt("2021-11-20T11:03:51.612Z")
                .respondentColor("#00ff00")
                .build();

        DialogueCommentOutput comment1 = DialogueCommentOutput
                .builder()
                .text("Kas me seda küsimust arutame?")
                .likesCount(2)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:00:00.612Z")
                .respondentColor("#ee0000")
                .build();


        DialogueCommentOutput comment2 = DialogueCommentOutput
                .builder()
                .text("O hi!")
                .likesCount(1)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:30:00.612Z")
                .respondentColor("#ee0000")
                .build();


        List<DialogueCommentOutput> commentsList1 = Arrays.asList(comment1, comment2);
        DialogueQuestionOutput question1 = DialogueQuestionOutput.builder()
                .title("First question title")
                .active(true)
                .sortOrder(1)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question2 = DialogueQuestionOutput.builder()
                .title("Another question title")
                .active(true)
                .sortOrder(2)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question3 = DialogueQuestionOutput.builder()
                .title("Closed question title")
                .active(false)
                .sortOrder(2)
                // don't send any comments since the question is archived
                .comments(Collections.emptyList())
                .build();

        DialogueTipOutput tip1 = DialogueTipOutput.builder().text("Some tip will be here").build();
        DialogueTipOutput tip2 = DialogueTipOutput.builder().text("Lorem ipsum sit amet").build();

        return DialogueTopicOutput.builder()
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
                .activeTopicsCount(3)
                .likesCount(5)
                .answersCount(3)
                .build();
    }
}
